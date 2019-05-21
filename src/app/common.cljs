(ns app.common
  (:require [cljs-bach.synthesis :as a]))

(defonce worker (atom (js/Worker. "/js/worker.js")))

(defn get-seconds-per-beat
  ([tempo] (/ 60.0 tempo))
  ([tempo divisions] (/ 60.0 tempo divisions)))

(defonce context (a/audio-context))
(defonce analyser-numerator (.createAnalyser context))
(defonce analyser-denominator (.createAnalyser context))
(defonce buffer-numerator (js/Uint8Array. 256))
(defonce buffer-denominator (js/Uint8Array. 256))

(defn init-audio
  []
  (set! (.-fftSize analyser-numerator) 512)
  (set! (.-fftSize analyser-denominator) 512))

(defn get-context-current-time
  []
  (a/current-time context))

(defonce grid-x (atom {:start nil :width nil}))
