(ns app.sound
  (:require [cljs-bach.synthesis :as a]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch dispatch-sync subscribe]]
            [app.common :refer [worker get-seconds-per-beat context]]
            [app.animation :refer [raf-id animate]]))

(defonce queue (atom '()))
(defonce start-time (atom 0))
(defonce first-run? (atom true))

(defonce schedule-ahead-time 0.1) ; s
(defonce lookahead 25.0) ; ms

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

(defn play-once [time freq which]
  (let [analyser (condp = which
                   :numerator analyser-numerator
                   :denominator analyser-denominator)]
    (-> (blip freq)
        (a/connect-> analyser a/destination)
        (a/run-with context time 0.1))))

(defn next-note [which]
  (let [tempo @(subscribe [:tempo])
        divisions @(subscribe [(keyword (str (name which) "-divisions"))])
        seconds-per-beat (get-seconds-per-beat tempo divisions)
        next-note-time @(subscribe [(keyword (str (name which) "-next-note-time"))])]
    (dispatch-sync [:change-next-note-time
                    {:next-note-time (+ next-note-time seconds-per-beat)
                     :which which}])))

(defn schedule-note [time freq which]
  (play-once time freq which))

(defonce current-micro-beat (r/atom 0))

(defn listen
  [query-v]
  @(subscribe query-v))

(defn scheduler []
  (let [tempo (listen [:tempo])
        last-beat-time (listen [:last-beat-time])
        numerator (listen [:numerator-divisions])
        denominator (listen [:denominator-divisions])
        seconds-per-numerator (get-seconds-per-beat tempo numerator)
        seconds-per-denominator (get-seconds-per-beat tempo denominator)
        limit (+ schedule-ahead-time (a/current-time context))]
    (while (< (-> @(subscribe [:numerator-microbeat])
                  (* seconds-per-numerator)
                  (+ last-beat-time))
              limit)
      (schedule-note
       (->
        (listen [:numerator-microbeat])
        (* seconds-per-numerator)
        (+ last-beat-time))
       3520
       :numerator)
      (dispatch-sync [:inc-microbeat :numerator]))
    (while (< (-> (listen [:denominator-microbeat])
                  (* seconds-per-denominator)
                  (+ last-beat-time))
              limit)
      (schedule-note
       (->
        (listen [:denominator-microbeat])
        (* seconds-per-denominator)
        (+ last-beat-time))
       2640
       :denominator)
      (dispatch-sync [:inc-microbeat :denominator]))
    (if (and
         (> (listen [:numerator-microbeat]) numerator)
         (> (listen [:denominator-microbeat]) denominator))
      (dispatch-sync [:normalize-microbeats]))))

; (defn scheduler []
;   (while (< @(subscribe [:numerator-next-note-time]) (+ schedule-ahead-time (a/current-time context)))
;     (do
;       (schedule-note @(subscribe [:numerator-next-note-time]) 2640)
;       (next-note :numerator)))
;   (while (< @(subscribe [:denominator-next-note-time]) (+ schedule-ahead-time (a/current-time context)))
;     (do
;       (schedule-note @(subscribe [:denominator-next-note-time]) 1320)
;       (next-note :denominator))))

(defn play
  []
  (let [is-playing? @(subscribe [:is-playing?])]
    (if (not is-playing?)
      (do
        (dispatch [:toggle-playing])
        (reset! raf-id (js/window.requestAnimationFrame animate))
        (when @first-run?
          (-> (blip 1320)
              (a/connect->
               (a/gain 0.0)
               a/destination)
              (a/run-with context (a/current-time context) 0.1))
          (swap! first-run? not))
        (.postMessage @worker "start")
        (dispatch-sync [:change-last-beat-time (a/current-time context)])
        (dispatch-sync [:reset-microbeats]))
      (do
        (dispatch [:toggle-playing])
        (app.animation/stop-animation)
        (.postMessage @worker "stop")))))