(ns app.polyrhythms.views
  (:require [reagent.core :as r]
            [reagent.impl.template :as rtpl]
            [app.polyrhythms.sound :refer [play]]
            [app.styles :refer [light-blue]]
            [app.mui :as mui]
            [app.polyrhythms.common :refer [lcm]]
            [app.polyrhythms.common :refer [context grid-x]]
            [app.polyrhythms.buttons :refer [play-button pause-button]]
            [app.polyrhythms.settings :refer [settings-container]]
            [stylefy.core :as stylefy :refer [use-style]]
            [re-frame.core :refer [subscribe dispatch]]
            [clojure.string :refer [join]]))

(defonce window-width (r/atom nil))

(defn- get-ticks-children-style
  [idx idy span num]
  (let [column-start (-> idx (* span) (+ 2))
        bottom?      (= 1 idy)]
    {:grid-column    (str column-start " / span " span)
     :grid-row       (inc idy)
     :border-left    (if (zero? idx)
                       "solid 2px black"
                       "solid 1px black")
     :border-right   (if (= idx (dec num))
                       "solid 2px black"
                       "solid 1px black")
     (if bottom? :border-bottom :border-top) "solid 1px black"}))

(defn- generate-ticks
  [ticks total which]
  (let [y (condp = which
            :numerator 1
            :denominator 2)
        span (/ total ticks)]
    [:<>
     (doall
      (for [x (range 0 ticks)]
        ^{:key (str y x)}
        [:div
         (use-style
          (get-ticks-children-style x y span ticks)
          {:id (str (dec y) x)})]))]))

(defn- get-minor-tick-style
  [idx is-verbose?]
  {:grid-column  (+ idx 2)
   :grid-row     (if is-verbose? "2" "3")
   :color        "#777777"
   :justify-self "start"
   :transform    "translate(-50%, -50%)"
   :line-height  "1.5rem"
   :text-shadow  (clojure.string/join ", " (take 30 (repeat "0 0 4px #ffffff")))
   :font-size    "0.8rem"
   :font-weight  "normal"
   :text-align   "center"})

(defn- generate-sequential-numbers
  [x numerator denominator]
  [:<>
   ^{:key "numerator"}  [:div (inc (mod x denominator))]
   ^{:key "sequential"} [:div (inc x)]
   ^{:key "denominator"} [:div (inc (mod x numerator))]])

(defn- generate-cyclical-numbers
  [x numerator _]
  [:div (inc (mod x numerator))])

(defn- generate-minor-ticks
  [ticks numerator denominator]
  (let [is-verbose? @(subscribe [:is-verbose?])
        display-fn (if is-verbose?
                     generate-sequential-numbers
                     generate-cyclical-numbers)]
    [:<>
     (doall
      (for [x (range 0 ticks)]
        ^{:key (str "minor" x)}
        [:div
         (use-style (get-minor-tick-style x is-verbose?))
         (display-fn x numerator denominator)]))]))

(defn- get-number-style
  [idx idy span]
  (let [column-start (-> idx (* span) (+ 2))]
    {:grid-column  (str column-start " / span " span)
     :grid-row     (inc idy)
     :justify-self "start"
     :padding      "1rem 0"
     :font-size    "1.2rem"
     :line-height  "1.2rem"
     :transform    "translateX(-50%)"}))

(defn- increment-cyclical
  [modulus amount x]
  (inc (mod (* amount x) modulus)))

(defn- generate-numbers
  [args]
  (let [{:keys [ticks
                total
                which
                class]} args
        y (condp = which
            :numerator   0
            :denominator 3)
        span (/ total ticks)
        inc-fn (if
                (not @(subscribe [:is-verbose?]))
                 (if (= :numerator which)
                   (partial increment-cyclical ticks span)
                   (constantly 1))
                 inc)]
    [:<>
     (doall
      (for [x (range 0 ticks)]
        ^{:key (str "number" y x)}
        [:div
         (use-style (get-number-style x y span) {:class class})
         (inc-fn x)]))]))

(defn- get-visual-beep-container-style
  [vert-pos horiz-pos divisions]
  (let [which-row    (condp = vert-pos
                       :top 1
                       :bottom 4)
        which-column (condp = horiz-pos
                       :left 1
                       :right (+ 2 divisions))]
    {:grid-row        which-row
     :grid-column     which-column
     :display         "flex"
     :justify-content "center"
     :align-items     "center"}))

(def visual-beep-style
  {:width             "0.5rem"
   :height            "0.5rem"
   :border-radius     "0.5rem"
   :background-color  "#dddddd"})

(defn- generate-visual-beep
  [divisions]
  [:<>
   (doall
    (for [vert  [:top :bottom]
          horiz [:left :right]]
      ^{:key (str "beep" vert horiz)}
      [:div
       (use-style
        (get-visual-beep-container-style vert horiz divisions))
       [:div
        (use-style
         visual-beep-style
         {:class (str "beep " (name vert))})]]))])

(def selector-style
  {:flex "0 1 auto"})

(def mui-override-style
  [[:label.Mui-focused {:color light-blue}]
   [:.MuiOutlinedInput-root.Mui-focused
    [:.MuiOutlinedInput-notchedOutline {:border-color light-blue
                                        :color light-blue}]]])

(def input-style
  {:margin          "0 1rem"
   :width           "6rem"
   :scrollbar-width "thin"
   ::stylefy/manual mui-override-style})

(defn desktop-number-input
  [type value]
  [mui/text-field
   (use-style
    input-style
    {:type  "number"
     :label (str (name type) ":")
     :name  (name type)
     :value value
     :min   1
     :margin "dense"
     :variant "outlined"
     :onChange #(dispatch [:change-divisions
                           {:divisions (.. % -target -value)
                            :which type}])})])

(def option-style
  {:padding "0 1rem"})

(defn mobile-number-select
  [type value]
  [mui/text-field
   (use-style
    input-style
    {:select      true
     :value       value
     :label       type
     :variant     "outlined"
     :margin      "dense"
     :SelectProps #js {:native true
                       :autoWidth true}
     :onChange   #(dispatch
                   [:change-divisions
                    {:divisions (.. % -target -value)
                     :which type}])})
   (doall
    (for [n (range 1 100)]
      ^{:key (str "select " n)}
      [:option (use-style option-style) n]))])

(defn selector
  [type value]
  [:div (use-style selector-style)
   (if @(subscribe [:is-mobile?])
     (mobile-number-select type value)
     (desktop-number-input type value))])

(def control-group-style
  {:display         "flex"
   :justify-content "space-evenly"
   :margin-bottom   "2rem"})

(defn control-group
  [numerator denominator total-divisions]
  [:div (use-style control-group-style)
   (selector :numerator numerator)
   [mui/text-field
    {:type "number"
     :label "least common multiple"
     :value total-divisions
     :variant "outlined"
     :margin "dense"
     :disabled true}]
   (selector :denominator denominator)])

(def tempo-group-style
  {:display         "flex"
   :justify-content "center"
   ::stylefy/manual mui-override-style})

(def tempo-input-style
  {:margin "0 1rem"})

(defn tempo-control []
  (fn []
    [:div
     (use-style tempo-group-style)
     [mui/text-field
      (use-style
       tempo-input-style
       {:key       "tempo"
        :type      "number"
        :variant   "outlined"
        :margin    "dense"
        :label     "tempo"
        :name      "tempo"
        :value     (str @(subscribe [:tempo]))
        :onWheel  #(let [del    (.-deltaY %)
                         tempo @(subscribe [:tempo])]
                     (cond
                       (pos? del) (dispatch [:change-tempo (dec tempo)])
                       (neg? del) (dispatch [:change-tempo (inc tempo)])))
        :onChange #(dispatch [:change-tempo (.. % -target -value)])
        :onBlur   #(dispatch [:change-tempo (.. % -target -value)])})]]))

(def container-style
  {:margin-top       (str (+ 32 app.styles/navbar-height) "px")
   :padding-bottom   "5rem"
   :background-color "#fafafa"
   :box-shadow       "2px 2px 5px rgba(0,0,0,0.6)"
   :border-radius    "5px"
   :font-family      "lato-light, sans-serif"})

(def metronome-group-style
  {:padding "0 5rem"})

(defn- get-grid-style
  [least-common-multiple]
  {:display               "grid"
   :grid-template-columns (str "repeat(" (+ least-common-multiple 2) ", 1fr)")
   :grid-template-rows    (str "3.2rem 1.5rem 1.5rem 3.2rem")})

(defn handle-play-click
  [event]
  (if (= (.-state context) "suspended")
    (-> (.resume context) (.then (play)))
    (play)))

(def cursor-style
  {:background-color "#00BBBB"
   :border           "none"
   :box-shadow       "none"
   :opacity          "0.5"
   :position         "absolute"
   :margin           "0"})

(defn rerender-cursor
  [ref num-divisions cursor-width]
  (when (some? ref)
    (let [ref         ref
          el-00-rec   (.getBoundingClientRect (js/document.getElementById "00"))
          start-x     (- (.-left el-00-rec) (/ cursor-width 2))
          width-x     (* (.-width el-00-rec) num-divisions)
          grid-el-rec (.getBoundingClientRect (js/document.getElementById "grid"))
          height      (.-height grid-el-rec)
          start-y     (+ (.-top grid-el-rec) (.-scrollY js/window))]
      (swap! grid-x assoc :start start-x :width width-x)
      (set! (.. ref -style -left) (str start-x "px"))
      (set! (.. ref -style -top) (str start-y "px"))
      (.setAttribute ref "size" height))))

(defn cursor
  [num-divisions]
  (let [!ref (atom nil)
        cursor-width 4]
    (r/create-class
     {:component-did-mount #(rerender-cursor
                             @!ref num-divisions cursor-width)
      :component-did-update #(rerender-cursor
                              @!ref num-divisions cursor-width)
      :display-name "cursor"
      :reagent-render (fn []
                        @window-width
                        @(subscribe [:numerator-divisions])
                        @(subscribe [:denominator-divisions])
                        [:hr
                         (use-style
                          cursor-style
                          {:id    "cursor"
                           :width cursor-width
                           :size  "800"
                           :ref   #(reset! !ref %)})])})))

(stylefy/tag "body"
             {:overflow-y "auto !important"
              :padding-right "0 !important"})

(defn polyrhythm-container
  []
  (let [numerator       @(subscribe [:numerator-divisions])
        denominator     @(subscribe [:denominator-divisions])
        total-divisions  (lcm numerator denominator)
        is-playing?     @(subscribe [:is-playing?])]
    [mui/paper (use-style container-style
                          {:elevation 3})
     [settings-container]
     [:div (use-style metronome-group-style)
      [cursor numerator]
      [control-group numerator denominator total-divisions]
      [:div (use-style (get-grid-style total-divisions) {:id "grid"})
       [generate-numbers {:ticks numerator
                          :total total-divisions
                          :which :numerator
                          :class "number numerator"}]
       [generate-ticks numerator total-divisions :numerator]
       [generate-ticks denominator total-divisions :denominator]
       [generate-numbers {:ticks denominator
                          :total total-divisions
                          :which :denominator
                          :class "number denominator"}]
       [generate-minor-ticks total-divisions numerator denominator]
       [generate-visual-beep total-divisions]]
      [:div
       (use-style {:text-align "center"})
       (if is-playing?
         [pause-button handle-play-click]
         [play-button  handle-play-click])]
      [tempo-control]]]))