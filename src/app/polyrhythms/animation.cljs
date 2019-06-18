(ns app.polyrhythms.animation
  (:require [reagent.core :as r]
            [app.polyrhythms.common :refer [get-seconds-per-beat
                                            get-context-current-time
                                            grid-x
                                            populate-analyser
                                            buffer-numerator
                                            buffer-denominator]]
            [re-frame.core :refer [subscribe]]))

(defonce raf-id (r/atom nil))

(defn set-el-highlight
  [el color]
  (set! (.. el -style -fontWeight) "bold")
  (set! (.. el -style -fontSize) "1.5rem")
  (set! (.. el -style -textShadow) color)
  (set! (.. el -style -color) "#ffffff"))

(defn unset-el-highlight
  [el]
  (set! (.. el -style -fontWeight) "normal")
  (set! (.. el -style -fontSize) "1.2rem")
  (set! (.. el -style -textShadow) "none")
  (set! (.. el -style -color) "#000000"))

(defn do-beep
  [which]
  (let [[buffer color]    (condp = which
                            :top    [buffer-numerator "#ff3333"]
                            :bottom [buffer-denominator "#3333ff"])
        normalized-buffer (reduce (fn [acc val]
                                    (+ acc (Math/abs (- val 128))))
                                  0
                                  (array-seq buffer))]
    (doseq
     [el (array-seq (js/document.getElementsByClassName (str "beep " (name which))))]
      (if (pos? normalized-buffer)
        (set! (.. el -style -backgroundColor) color)
        (set! (.. el -style -backgroundColor) "#dddddd")))))

(defn animate []
  (reset! raf-id (js/window.requestAnimationFrame animate))
  (let [{:keys [start width]} @grid-x
        last-beat-time        @(subscribe [:last-beat-time])
        seconds-per-beat      (get-seconds-per-beat @(subscribe [:tempo]))
        time-since-last-beat  (- (get-context-current-time) last-beat-time)
        progress              (/ time-since-last-beat seconds-per-beat)
        progress-adjusted     (if (neg? progress) (inc progress) progress)
        cursor                (js/document.getElementById "cursor")
        cursor-bounding       (.getBoundingClientRect cursor)
        cursor-left           (.-left cursor-bounding)
        cursor-width          (.-width cursor-bounding)
        cursor-midpoint       (+ cursor-left (/ cursor-width 2))
        new-x                 (+ start (* progress-adjusted width))]
    (set! (.. cursor -style -left) (str (max start new-x) "px"))
    (doseq [el (array-seq (js/document.getElementsByClassName "number"))
            :let [el-bounding         (.getBoundingClientRect el)
                  el-left             (.-left el-bounding)
                  el-width            (.-width el-bounding)
                  el-midpoint         (+ el-left (/ el-width 2))
                  midpoint-difference (- cursor-midpoint el-midpoint)
                  max-thresh          (- width 20)
                  color               (if
                                       (-> el .-classList (.contains "denominator"))
                                        (clojure.string/join
                                         ", "
                                         (take 10
                                               (repeat "0 0 3px rgba(51,51,255,0.2)")))
                                        (clojure.string/join
                                         ", "
                                         (take 10
                                               (repeat "0 0 3px rgba(255,51,51,0.2)"))))]]
      (if (or (and (> midpoint-difference -20)
                   (< midpoint-difference 50))
              (> midpoint-difference max-thresh))
        (set-el-highlight el color)
        (unset-el-highlight el)))
    (populate-analyser :numerator)
    (populate-analyser :denominator)
    (do-beep :top)
    (do-beep :bottom)))

(defn stop-animation []
  (js/window.cancelAnimationFrame @raf-id)
  (doseq [el (array-seq (js/document.querySelectorAll ".number,.beep"))]
    (.removeAttribute el "style"))
  (let [{:keys [start width]} @grid-x
        cursor                (js/document.getElementById "cursor")]
    (set! (.. cursor -style -left) (str start "px")))
  (reset! raf-id nil))