(ns app.sound
  (:require [cljs-bach.synthesis :as a]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [app.common :refer [worker]]))

(defonce context (a/audio-context))
(defonce queue (atom '()))
(defonce start-time (atom 0))
(defonce first-run? (atom true))

(defonce schedule-ahead-time 0.1) ; s
(defonce lookahead 25.0) ; ms

(defn blip [freq]
  (a/connect->
   (a/square freq)
   (a/percussive 0.01 0.05)
   (a/gain 0.2)))

(defn play-once [time freq]
  (-> (blip freq)
      (a/connect-> a/destination)
      (a/run-with context time 0.1)))

(defn next-note [which]
  (let [tempo @(subscribe [:tempo])
        divisions @(subscribe [(keyword (str (name which) "-divisions"))])
        seconds-per-beat (/ 60.0 tempo divisions)
        next-note-time @(subscribe [(keyword (str (name which) "-next-note-time"))])]
    (dispatch-sync [:change-next-note-time
                    {:next-note-time (+ next-note-time seconds-per-beat)
                     :which which}])))

(defn schedule-note [time freq]
  (play-once time freq))

(defn scheduler []
  (while (< @(subscribe [:numerator-next-note-time]) (+ schedule-ahead-time (a/current-time context)))
    (do
      (schedule-note @(subscribe [:numerator-next-note-time]) 2640)
      (next-note :numerator)))
  (while (< @(subscribe [:denominator-next-note-time]) (+ schedule-ahead-time (a/current-time context)))
    (do
      (schedule-note @(subscribe [:denominator-next-note-time]) 1320)
      (next-note :denominator))))

(defn play
  []
  (let [is-playing? @(subscribe [:is-playing?])]
    (dispatch [:toggle-playing])
    (if (not is-playing?)
      (do
        (when @first-run?
          (-> (blip 1320)
              (a/connect->
               (a/gain 0.0)
               a/destination)
              (a/run-with context (a/current-time context) 0.1))
          (swap! first-run? not))
        (dispatch-sync [:change-all-next-note-times (a/current-time context)])
        (.postMessage @worker "start")
        (reset! start-time (a/current-time context)))
      (.postMessage @worker "stop"))))