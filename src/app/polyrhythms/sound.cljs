(ns app.polyrhythms.sound
  (:require [cljs-bach.synthesis :as a]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [app.common :refer [worker get-seconds-per-beat context]]
            [app.animation :refer [raf-id animate]]))

(defonce schedule-ahead-time 0.1) ; s
(defonce lookahead 25.0) ; ms
(defonce beat-frequencies {:numerator 3520 :denominator 2640})

(defonce audio-filter (a/connect->
                       (a/percussive 0.01 0.05)
                       (a/gain 0.2)))

(defn analyser-numerator
  []
  (a/subgraph app.common/analyser-numerator))

(defn analyser-denominator
  []
  (a/subgraph app.common/analyser-denominator))

(defn blip [freq]
  (a/connect->
   (a/sine freq)
   audio-filter))

(defn listen
  [query-v]
  @(subscribe query-v))

(defn play-once [time which]
  (let [analyser (condp = which
                   :numerator analyser-numerator
                   :denominator analyser-denominator)]
    (-> (blip (which beat-frequencies))
        (a/connect-> analyser a/destination)
        (a/run-with context time 0.1))))

(defn next-note [which]
  (let [tempo (listen [:tempo])
        divisions (listen [(keyword (str (name which) "-divisions"))])
        seconds-per-beat (get-seconds-per-beat tempo divisions)
        next-note-time (listen [(keyword (str (name which) "-next-note-time"))])]
    (dispatch-sync [:change-next-note-time
                    {:next-note-time (+ next-note-time seconds-per-beat)
                     :which which}])))

(defn schedule-note [time which]
  (play-once time which))

(defn scheduler-loop
  [which seconds-per last-beat-time limit]
  (let [which-microbeat (condp = which
                          :numerator :numerator-microbeat
                          :denominator :denominator-microbeat)
        get-next-note-time (fn []
                             (-> (listen [which-microbeat])
                                 (* seconds-per)
                                 (+ last-beat-time)))]
    (while (< (get-next-note-time) limit)
      (schedule-note (get-next-note-time) which)
      (dispatch-sync [:inc-microbeat which]))))

(defn scheduler []
  (let [tempo (listen [:tempo])
        last-beat-time (listen [:last-beat-time])
        numerator (listen [:numerator-divisions])
        denominator (listen [:denominator-divisions])
        seconds-per-numerator (get-seconds-per-beat tempo numerator)
        seconds-per-denominator (get-seconds-per-beat tempo denominator)
        limit (+ schedule-ahead-time (a/current-time context))]
    (scheduler-loop :numerator seconds-per-numerator last-beat-time limit)
    (scheduler-loop :denominator seconds-per-denominator last-beat-time limit)
    (if (and
         (> (listen [:numerator-microbeat]) numerator)
         (> (listen [:denominator-microbeat]) denominator))
      (dispatch-sync [:normalize-microbeats]))))

(defn play
  []
  (let [is-playing? (listen [:is-playing?])]
    (if (not is-playing?)
      (do
        (dispatch [:toggle-playing])
        (reset! raf-id (js/window.requestAnimationFrame animate))
        (.postMessage ^js @worker "start")
        (dispatch-sync [:change-last-beat-time (+ 0.06 (a/current-time context))])
        (dispatch-sync [:reset-microbeats]))
      (do
        (dispatch [:toggle-playing])
        (app.animation/stop-animation)
        (.postMessage ^js @worker "stop")))))