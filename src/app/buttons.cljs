(ns app.buttons
  (:require [reagent.core :as r]
            [stylefy.core :as stylefy :refer [use-style]]
            [clojure.string :refer [join]]))

(defn map-fn
  [func]
  #(map func %))

(def hover-style
  {:transition "all 0.2s"
   ::stylefy/mode
   {:hover
    {:cursor "pointer"
     :transform "scale(1.05)"
     :fill "#888888"}}})

(defn play-button
  [handle-click]
  [:svg
   {:width   120
    :height  120
    :viewBox "-60 -60 120 120"
    :xlms    "http://www.w3.org/2000/svg"}
   [:path
    (use-style
     hover-style
     {:d
      (str
       "M"
       (doall
        (let [angles (range 0 (* 2 Math/PI) (-> Math/PI (* 2) (/ 3)))
              xy     ((juxt
                       (fn
                         [angle]
                         (map #(->> % Math/cos (* 50)) angle))
                       (fn
                         [angle]
                         (map #(->> % Math/sin (* 50)) angle)))
                      angles)]
          (clojure.string/join
           "L"
           (apply map #(clojure.string/join " " [%1 %2]) xy))))
       "Z")
      :fill "#000000"
      :onClick handle-click})]])

(defn pause-button
  [handle-click]
  [:svg
   {:width 120
    :height 120
    :viewBox "-60 -60 120 120"
    :xlms "http://www.w3.org/2000/svg"}
   [:g
    (use-style hover-style {:onClick handle-click})
    [:rect
     {:x "-30"
      :y "-40"
      :height "60"
      :width "80"
      :fill "transparent"}]
    [:path
     {:d "M-30 -40L-30 40L-10 40L-10 -40Z"}]
    [:path
     {:d "M30 40L30 -40L10 -40L10 40Z"}]]])