(ns app.polyrhythms.common
  (:require [cljs-bach.synthesis :as a]))

(defonce worker (atom (js/Worker. "./js/worker.js")))

(defn get-seconds-per-beat
  ([tempo] (/ 60.0 tempo))
  ([tempo divisions] (/ 60.0 tempo divisions)))

(defonce context (a/audio-context))
(defonce analyser-numerator (.createAnalyser ^js context))
(defonce analyser-denominator (.createAnalyser ^js context))
(defonce buffer-numerator (js/Uint8Array. 256))
(defonce buffer-denominator (js/Uint8Array. 256))

(defn init-audio
  []
  (set! (.-fftSize analyser-numerator) 512)
  (set! (.-fftSize analyser-denominator) 512))

(defn get-context-current-time
  []
  (a/current-time context))

(defn populate-analyser
  [which]
  (let [analyser (condp = which
                   :numerator analyser-numerator
                   :denominator analyser-denominator)
        buffer (condp = which
                 :numerator buffer-numerator
                 :denominator buffer-denominator)]
    (.getByteTimeDomainData analyser buffer)))

(defonce grid-x (atom {:start nil :width nil}))

(defn gcd
  [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn lcm
  [a b]
  (/ (* a b) (gcd a b)))