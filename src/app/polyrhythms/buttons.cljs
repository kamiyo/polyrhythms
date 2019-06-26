(ns app.polyrhythms.buttons
  (:require [reagent.core :as r]
            [stylefy.core :as stylefy :refer [use-style]]
            [app.styles :refer [dark-blue light-blue]]
            [clojure.string :refer [join]]))

(defn map-fn
  [func]
  #(map func %))

(def button-style
  {:transition "all 0.2s"
   :fill dark-blue
   ::stylefy/mode
   {:hover
    {:cursor "pointer"
     :transform "scale(1.05)"
     :fill light-blue}}})

(defn play-button
  [props]
  (let [on-click (:on-click props)
        rest (dissoc props :on-click)]
    [:svg
     (merge rest
            {:viewBox "-60 -60 120 120"
             :xlms    "http://www.w3.org/2000/svg"})
     [:path
      (use-style
       button-style
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
        :on-click on-click})]]))

(defn pause-button
  [props]
  (let [on-click (:on-click props)
        rest (dissoc props :on-click)]
    [:svg
     (merge rest
            {:viewBox "-60 -60 120 120"
             :xlms "http://www.w3.org/2000/svg"})
     [:g
      (use-style button-style {:onClick on-click})
      [:rect
       {:x "-30"
        :y "-40"
        :height "60"
        :width "80"
        :fill "transparent"}]
      [:path
       {:d "M-30 -40L-30 40L-10 40L-10 -40Z"}]
      [:path
       {:d "M30 40L30 -40L10 -40L10 40Z"}]]]))