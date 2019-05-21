(ns app.animation
  (:require [reagent.core :as r]
            [app.common :refer [get-seconds-per-beat get-context-current-time grid-x]]
            [re-frame.core :refer [subscribe]]))

(defonce raf-id (r/atom nil))

(defn animate
  []
  (reset! raf-id (js/window.requestAnimationFrame animate))
  (let [{:keys [start width]} @grid-x
        last-beat-time @(subscribe [:last-beat-time])
        seconds-per-beat (get-seconds-per-beat @(subscribe [:tempo]))
        time-since-last-beat (- (get-context-current-time) last-beat-time)
        progress (/ time-since-last-beat seconds-per-beat)
        progress-adjusted (if (neg? progress) (+ 1 progress) progress)
        new-x (+ start (* progress-adjusted width))
        cursor (js/document.getElementById "cursor")
        cursor-left (-> cursor (.getBoundingClientRect) .-left)
        cursor-width (-> cursor (.getBoundingClientRect) .-width)
        cursor-midpoint (+ cursor-left (/ cursor-width 2))]
    (set! (-> cursor .-style .-left) (str new-x "px"))
    (doseq [el (array-seq (js/document.getElementsByClassName "number"))
            :let [el-left (-> el (.getBoundingClientRect) .-left)
                  el-width (-> el (.getBoundingClientRect) .-width)
                  el-midpoint (+ el-left (/ el-width 2))
                  midpoint-difference (- cursor-midpoint el-midpoint)
                  max-thresh (- width 20)
                  color (if (-> el .-classList (.contains "denominator"))
                          (clojure.string/join ", " (take 10 (repeat "0 0 3px rgba(51,51,255,0.2)")))
                          (clojure.string/join ", " (take 10 (repeat "0 0 3px rgba(255,51,51,0.2)"))))]]
      (if
       (or (and (> midpoint-difference -20)
                (< midpoint-difference 50))
           (> midpoint-difference max-thresh))
        (do (set! (-> el .-style .-fontWeight) "bold")
            (set! (-> el .-style .-fontSize) "1.5rem")
            (set! (-> el .-style .-textShadow) color)
            (set! (-> el .-style .-color) "#ffffff"))
        (do (set! (-> el .-style .-fontWeight) "normal")
            (set! (-> el .-style .-fontSize) "1.2rem")
            (set! (-> el .-style .-textShadow) "none")
            (set! (-> el .-style .-color) "#000000"))))
    (.getByteTimeDomainData app.common/analyser-numerator app.common/buffer-numerator)
    (.getByteTimeDomainData app.common/analyser-denominator app.common/buffer-denominator)
    (let [normalized-buffer (reduce #(+ %1 (-> %2 (- 128) Math/abs)) 0 (array-seq app.common/buffer-numerator))]
      (doseq [el (array-seq (js/document.getElementsByClassName "beep top"))]
        (if (> normalized-buffer 0)
          (set! (-> el .-style .-backgroundColor) "#ff3333")
          (set! (-> el .-style .-backgroundColor) "#dddddd"))))
    (let [normalized-buffer (reduce #(+ %1 (-> %2 (- 128) Math/abs)) 0 (array-seq app.common/buffer-denominator))]
      (doseq [el (array-seq (js/document.getElementsByClassName "beep bottom"))]
        (if (> normalized-buffer 0)
          (set! (-> el .-style .-backgroundColor) "#3333ff")
          (set! (-> el .-style .-backgroundColor) "#dddddd"))))))

(defn stop-animation
  []
  (js/window.cancelAnimationFrame @raf-id)
  (doseq [el (array-seq (js/document.querySelectorAll ".number,.beep"))]
    (.removeAttribute el "style"))
  (reset! raf-id nil))