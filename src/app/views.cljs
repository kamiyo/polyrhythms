(ns app.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [stylefy.core :as stylefy :refer [use-style]]
            [clojure.string :refer [join]]
            [promesa.core :as p]
            [app.buttons]))

(defn gcd
  [a b]
  (if (zero? b)
    a
    (recur b (mod a b))))

(defn lcm
  [a b]
  (/ (* a b) (gcd a b)))

(defn- get-children-style
  [idx idy span num]
  {:grid-column (str (-> idx (* span) (+ 2)) " / span " span)
   :grid-row (+ idy 1)
   :border-left (if (= 0 idx) "solid 2px black" "solid 1px black")
   :border-right (if (= idx (- num 1)) "solid 2px black" "solid 1px black")
   (if (= 1 idy) :border-bottom :border-top) "solid 1px black"})

(defn- generate-ticks
  [ticks total which]
  (let
   [y (condp = which
        :numerator 1
        :denominator 2)
    span (/ total ticks)]
    (doall (for [x (range 0 ticks)]
             ^{:key (str y x)}
             [:div
              (use-style
               (get-children-style x y span ticks))]))))

(defn- get-minor-tick-style
  [idx]
  {:grid-column (+ idx 2)
   :grid-row "2"
   :color "#b0b0b0"
   :justify-self "start"
   :transform "translate(-50%, -50%)"
   :line-height "1.5rem"
   :text-shadow (clojure.string/join ", " (take 20 (repeat "0 0 3px #ffffff")))
   :font-size "0.8rem"
   :font-weight "normal"})

(defn- generate-minor-ticks
  [ticks numerator denominator]
  (doall (for [x (range 0 ticks)]
           ^{:key (str "minor" x)}
           [:div
            (use-style (get-minor-tick-style x))
            [:div (-> x (mod denominator) (+ 1))]
            [:div (+ x 1)]
            [:div (-> x (mod numerator) (+ 1))]])))

(defn- get-number-style
  [idx idy span]
  {:grid-column (str (-> idx (* span) (+ 2)) " / span " span)
   :grid-row (+ idy 1)
   :justify-self "start"
   :padding "1rem 0"
   :font-size "1.2rem"
   :transform "translateX(-50%)"})

(defn- generate-numbers
  [ticks total which]
  (let
   [y (condp = which
        :numerator 0
        :denominator 3)
    span (/ total ticks)]
    (doall (for [x (range 0 ticks)]
             ^{:key (str "number" y x)}
             [:div
              (use-style (get-number-style x y span))
              (+ x 1)]))))

(def selector-style
  {:flex "0 1 auto"})

(def input-style
  {:margin "0 1rem"})

(defn selector
  [type value]
  [:div (use-style selector-style)
   (str (name type) ": ")
   [:input
    (use-style
     input-style
     {:type "number"
      :name (name type)
      :value value
      :min 1
      :onChange #(dispatch [:change-divisions
                            {:divisions (-> % .-target .-value)
                             :which type}])})]])

(def control-group-style
  {:display "flex"
   :justify-content "space-evenly"})

(defn control-group
  [numerator denominator total-divisions]
  [:div (use-style control-group-style)
   (selector :numerator numerator)
   "gcm: " total-divisions
   (selector :denominator denominator)])

(defn tempo-control
  []

  (fn []
    [:input
     {:key "tempo"
      :type "number"
      :name "tempo"
      :value (str @(subscribe [:tempo]))
      :onWheel #(let [del (.. % -deltaY)
                      tempo @(subscribe [:tempo])]
                  (cond
                    (pos? del) (dispatch [:change-tempo (- 1 tempo)])
                    (neg? del) (dispatch [:change-tempo (+ 1 tempo)])))
      :onChange #(dispatch [:change-tempo (.. % -target -value)])
      :onBlur #(dispatch [:change-tempo (.. % -target -value)])}]))

(def container-style
  {:padding "5em"
   :background-color "#fafafa"
   :box-shadow "2px 2px 5px rgba(0,0,0,0.6)"
   :border-radius "5px"
   :font-family "Roboto, sans-serif"})

(defn- get-grid-style
  [least-common-multiple]
  {:display "grid"
   :grid-template-columns (str "repeat(" (+ least-common-multiple 2) ", 1fr)")
   :grid-template-rows (str "3rem 1.5rem 1.5rem 3rem")})

(defn handle-click
  [event]
  (if (= (.-state app.sound/context) "suspended")
    (-> (.resume app.sound/context) (p/then #(app.sound/play)))
    (app.sound/play)))

(defn container
  []
  (let [numerator @(subscribe [:numerator-divisions])
        denominator @(subscribe [:denominator-divisions])
        total-divisions (lcm numerator denominator)
        is-playing? @(subscribe [:is-playing?])]
    [:div (use-style container-style)
     (control-group numerator denominator total-divisions)
     [:div (use-style (get-grid-style total-divisions))
      (generate-numbers numerator total-divisions :numerator)
      (generate-ticks numerator total-divisions :numerator)
      (generate-ticks denominator total-divisions :denominator)
      (generate-numbers denominator total-divisions :denominator)
      (generate-minor-ticks total-divisions numerator denominator)]
     [:div
      {:style {:text-align "center"}}
      (if is-playing?
        (app.buttons/pause-button handle-click)
        (app.buttons/play-button handle-click))]
     [(tempo-control)]]))

(defn app
  []
  [container])