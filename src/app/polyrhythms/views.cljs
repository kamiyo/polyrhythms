(ns app.polyrhythms.views
  (:require [reagent.core :as r]
            [app.polyrhythms.sound :refer [play]]
            [app.common :refer [context grid-x]]
            [app.buttons]
            [stylefy.core :as stylefy :refer [use-style]]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :refer [join]]))

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
  (let [column-start (-> idx (* span) (+ 2))
        bottom? (= 1 idy)]
    {:grid-column (str column-start " / span " span)
     :grid-row (inc idy)
     :border-left (if (zero? idx)
                    "solid 2px black"
                    "solid 1px black")
     :border-right (if (= idx (dec num))
                     "solid 2px black"
                     "solid 1px black")
     (if bottom? :border-bottom :border-top) "solid 1px black"}))

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
               (get-children-style x y span ticks)
               {:id (str (dec y) x)})]))))

(defn- get-minor-tick-style
  [idx is-cyclical?]
  {:grid-column (+ idx 2)
   :grid-row (if is-cyclical? "3" "2")
   :color "#b0b0b0"
   :justify-self "start"
   :transform "translate(-50%, -50%)"
   :line-height "1.5rem"
   :text-shadow (clojure.string/join ", " (take 20 (repeat "0 0 3px #ffffff")))
   :font-size "0.8rem"
   :font-weight "normal"
   :text-align "center"})

(defn- generate-number-column
  [x numerator denominator]
  [:<>
   [:div (-> x (mod denominator) inc)]
   [:div (inc x)]
   [:div (-> x (mod numerator) inc)]])

(defn- generate-minor-ticks
  [ticks numerator denominator]
  (let [display-type @(subscribe [:display-type])
        display-fn (condp = display-type
                     :sequential generate-number-column
                     :cyclical (fn
                                 [x numerator _]
                                 [:div (-> x (mod numerator) inc)]))]
    (doall (for [x (range 0 ticks)]
             ^{:key (str "minor" x)}
             [:div
              (use-style (get-minor-tick-style x (= display-type :cyclical)))
              (display-fn x numerator denominator)]))))

(defn- get-number-style
  [idx idy span]
  (let [column-start (-> idx (* span) (+ 2))]
    {:grid-column (str column-start " / span " span)
     :grid-row (inc idy)
     :justify-self "start"
     :padding "1rem 0"
     :font-size "1.2rem"
     :line-height "1.2rem"
     :transform "translateX(-50%)"}))

(defn- increment-cyclical
  [modulus amount x]
  (inc (mod (* amount x) modulus)))

(defn- generate-numbers
  [args]
  (let
   [{:keys [ticks total which class]} args
    y (condp = which
        :numerator 0
        :denominator 3)
    span (/ total ticks)
    inc-fn (if
            (and
             (= :cyclical @(subscribe [:display-type]))
             (= :numerator which))
             (partial increment-cyclical ticks span)
             inc)]
    (doall
     (for [x (range 0 ticks)]
       ^{:key (str "number" y x)}
       [:div
        (use-style (get-number-style x y span) {:class class})
        (inc-fn x)]))))

(defn- get-visual-beep-container-style
  [vert-pos horiz-pos divisions]
  (let [which-row (condp = vert-pos
                    :top 1
                    :bottom 4)
        which-column (condp = horiz-pos
                       :left 1
                       :right (+ 2 divisions))]
    {:grid-row which-row
     :grid-column which-column
     :display "flex"
     :justify-content "center"
     :align-items "center"}))

(def visual-beep-style
  {:width "0.5rem"
   :height "0.5rem"
   :border-radius "0.5rem"
   :background-color "#dddddd"})

(defn- generate-visual-beep
  [divisions]
  (doall
   (for [vert [:top :bottom]
         horiz [:left :right]]
     ^{:key (str "beep" vert horiz)}
     [:div
      (use-style
       (get-visual-beep-container-style vert horiz divisions))
      [:div
       (use-style
        visual-beep-style
        {:class (str "beep " (name vert))})]])))

(def selector-style
  {:flex "0 1 auto"})

(def input-style
  {:margin "0 1rem"
   :width "6rem"
   :scrollbar-width "thin"})

(defn desktop-number-input
  [type value]
  [:input
   (use-style
    input-style
    {:type "number"
     :name (name type)
     :value value
     :min 1
     :onChange #(dispatch [:change-divisions
                           {:divisions (-> % .-target .-value)
                            :which type}])})])

(def option-style
  {:padding "0 1rem"})

(defn mobile-number-select
  [type value]
  [:select
   (use-style
    input-style
    {:value value
     :onChange #(dispatch [:change-divisions
                           {:divisions (-> % .-target .-value)
                            :which type}])})
   (doall
    (for [n (range 1 100)]
      ^{:key (str "select " n)}
      [:option (use-style
                option-style
                {:value n})
       n]))])

(defn selector
  [type value]
  [:div (use-style selector-style)
   (str (name type) ": ")
   (js/console.log @(subscribe [:is-mobile?]))
   (if @(subscribe [:is-mobile?])
     (mobile-number-select type value)
     (desktop-number-input type value))])

(def control-group-style
  {:display "flex"
   :justify-content "space-evenly"
   :margin-bottom "2rem"})

(defn control-group
  [numerator denominator total-divisions]
  [:div (use-style control-group-style)
   (selector :numerator numerator)
   "lcm: " total-divisions
   (selector :denominator denominator)])

(def tempo-group-style
  {:display "flex"
   :justify-content "center"})

(def tempo-input-style
  {:margin "0 1rem"})

(defn tempo-control
  []
  (fn []
    [:div
     (use-style tempo-group-style)
     "tempo: "
     [:input
      (use-style
       tempo-input-style
       {:key "tempo"
        :type "number"
        :name "tempo"
        :value (str @(subscribe [:tempo]))
        :onWheel #(let [del (.. % -deltaY)
                        tempo @(subscribe [:tempo])]
                    (cond
                      (pos? del) (dispatch [:change-tempo (dec tempo)])
                      (neg? del) (dispatch [:change-tempo (inc tempo)])))
        :onChange #(dispatch [:change-tempo (.. % -target -value)])
        :onBlur #(dispatch [:change-tempo (.. % -target -value)])})]]))

(def container-style
  {:margin-top (str (+ 32 app.styles/navbar-height) "px")
   :padding "0 5em 5em 5em"
   :background-color "#fafafa"
   :box-shadow "2px 2px 5px rgba(0,0,0,0.6)"
   :border-radius "5px"
   :font-family "lato-light, sans-serif"})

(defn- get-grid-style
  [least-common-multiple]
  {:display "grid"
   :grid-template-columns (str "repeat(" (+ least-common-multiple 2) ", 1fr)")
   :grid-template-rows (str "3.2rem 1.5rem 1.5rem 3.2rem")})

(defn handle-click
  [event]
  (if (= (.-state context) "suspended")
    (-> (.resume context) (.then (play)))
    (play)))

(def cursor-style
  {:background-color "#00BBBB"
   :border "none"
   :box-shadow "none"
   :opacity "0.5"
   :position "absolute"
   :margin "0"})

(defn cursor
  [num-divisions]
  (let [!ref (atom nil)
        cursor-width 4]
    (r/create-class
     {:component-did-mount
      (fn []
        (when (some? @!ref)
          (let [ref         @!ref
                el-00-rec   (.getBoundingClientRect (js/document.getElementById "00"))
                start-x     (.-left el-00-rec)
                width-x     (* (.-width el-00-rec) num-divisions)
                grid-el-rec (.getBoundingClientRect (js/document.getElementById "grid"))
                height      (.-height grid-el-rec)
                start-y     (.-top grid-el-rec)]
            (swap! grid-x assoc :start start-x :width width-x)
            (set! (.. ref -style -left) (str start-x "px"))
            (set! (.. ref -style -top) (str start-y "px"))
            (.setAttribute ref "size" height))))
      :display-name "cursor"
      :reagent-render
      (fn []
        [:hr (use-style
              cursor-style
              {:id    "cursor"
               :width cursor-width
               :size  "800"
               :ref   #(reset! !ref %)})])})))

(def tabs-container-style
  {:display "flex"})

(defn- get-tab-style
  [is-active?]
  (let [color (if is-active? "black" "rgba(0,0,0,0.4)")]
    {:color color
     :flex           "1 1 auto"
     :display        "flex"
     :flex-direction "column"
     :text-align     "center"
     :margin         "0 0.5rem 2rem 0.5rem"
     :transition     "all 0.1s"
     ::stylefy/mode
     {:hover
      {:cursor "pointer"
       :text-shadow (str "0px 0px 2px " color)}}}))

(defn- get-tab-highlight
  [is-active?]
  {:width "100%"
   :height "5px"
   :background-color (if is-active? "rgb(78, 134, 164)" "none")})

(defn tabs
  []
  (let [display-type @(subscribe [:display-type])]
    [:div (use-style tabs-container-style)
     (doall (for [type app.db/display-types
                  :let [is-active? (= display-type type)]]
              [:div (use-style
                     (get-tab-style is-active?)
                     {:on-click #(dispatch [:change-display type])})
               [:div (use-style (get-tab-highlight is-active?))]
               [:div (use-style {:padding "1.4rem 2rem"})
                type]]))]))

(defn polyrhythm-container
  []
  (let [numerator @(subscribe [:numerator-divisions])
        denominator @(subscribe [:denominator-divisions])
        total-divisions (lcm numerator denominator)
        is-playing? @(subscribe [:is-playing?])]
    [:div (use-style container-style)
     (tabs)
     [(cursor numerator)]
     (control-group numerator denominator total-divisions)
     [:div (use-style (get-grid-style total-divisions) {:id "grid"})
      (generate-numbers {:ticks numerator
                         :total total-divisions
                         :which :numerator
                         :class "number numerator"})
      (generate-ticks numerator total-divisions :numerator)
      (generate-ticks denominator total-divisions :denominator)
      (generate-numbers {:ticks denominator
                         :total total-divisions
                         :which :denominator
                         :class "number denominator"})
      (generate-minor-ticks total-divisions numerator denominator)
      (generate-visual-beep total-divisions)]
     [:div
      {:style {:text-align "center"}}
      (if is-playing?
        (app.buttons/pause-button handle-click)
        (app.buttons/play-button handle-click))]
     [(tempo-control)]]))