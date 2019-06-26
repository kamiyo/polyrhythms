(ns app.polyrhythms.views
  (:require [reagent.core :as r]
            [reagent.impl.template :as rtpl]
            [app.styles :refer [navbar-height]]
            [app.mui :as mui]
            [app.polyrhythms.common :refer [lcm]]
            [app.polyrhythms.common :refer [grid-x]]
            [app.polyrhythms.settings :refer [settings-container]]
            [app.polyrhythms.visualizer :refer [visualizer-grid]]
            [app.polyrhythms.tempo :refer [tempo-play-group]]
            [app.polyrhythms.styles :refer [mui-override-style]]
            [stylefy.core :as stylefy :refer [use-style]]
            [re-frame.core :refer [subscribe dispatch]]))

(def selector-style
  {:flex "0 1 auto"})

(defn- input-style [is-mobile?]
  {:margin          "0 1rem"
   :width           "6rem"
   :scrollbar-width "thin"
   ::stylefy/manual (mui-override-style is-mobile?)})

(defn- desktop-number-input
  [type value]
  [mui/text-field
   (use-style
    (input-style false)
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
  {:padding "0"})

(defn mobile-number-select
  [type value]
  [mui/text-field
   (use-style
    (input-style true)
    {:select      true
     :value       value
     :label       type
     :variant     "outlined"
     :margin      "dense"
     :SelectProps #js {:native true
                       :autoWidth true}
     :onChange    #(dispatch
                    [:change-divisions
                     {:divisions (.. % -target -value)
                      :which type}])})
   (doall
    (for [n (range 1 100)]
      ^{:key (str "select " n)}
      [:option (use-style option-style) n]))])

(defn selector
  [type value is-mobile?]
  [:div (use-style selector-style)
   (if is-mobile?
     (mobile-number-select type value)
     (desktop-number-input type value))])

(defn- control-group-style [is-mobile?]
  {:display         "flex"
   :justify-content "space-evenly"
   :padding-top     "0.3rem"
   :margin-bottom   (if is-mobile? "0" "2rem")})

(defn- lcm-display-style [is-mobile?]
  {:margin-top      "0"
   :margin-bottom   "0"
   ::stylefy/manual (mui-override-style is-mobile?)})

(defn lcm-display [total-divisions is-mobile?]
  [mui/text-field
   (use-style
    (lcm-display-style is-mobile?)
    {:type "number"
     :label "least common multiple"
     :value total-divisions
     :variant "outlined"
     :margin "dense"
     :disabled true})])

(defn control-group
  [numerator denominator total-divisions is-mobile?]
  [:div (use-style (control-group-style is-mobile?))
   (selector :numerator numerator is-mobile?)
   (lcm-display total-divisions is-mobile?)
   (selector :denominator denominator is-mobile?)])

(def cursor-style
  {:background-color "#00BBBB"
   :border           "none"
   :box-shadow       "none"
   :opacity          "0.5"
   :position         "fixed"
   :margin           "0"})

(defn rerender-cursor
  [ref num-divisions cursor-width]
  (js/window.setTimeout
   #(when (some? ref)
      (let [ref         ref
            el-00-rec   (.getBoundingClientRect (js/document.getElementById "00"))
            start-x     (- (.-left el-00-rec) (/ cursor-width 2))
            width-x     (* (.-width el-00-rec) @num-divisions)
            grid-el-rec (.getBoundingClientRect (js/document.getElementById "grid"))
            height      (.-height grid-el-rec)
            start-y     (+ (.-top grid-el-rec) (.-scrollY js/window))]
        (js/console.log "rerender" start-x)
        (swap! grid-x assoc :start start-x :width width-x)
        (set! (.. ref -style -left) (str start-x "px"))
        (set! (.. ref -style -top) (str start-y "px"))
        (.setAttribute ref "size" height)))
   0))

(defn cursor []
  (let [!ref          (atom nil)
        num-divisions (subscribe [:numerator-divisions])
        cursor-width  4]
    (r/create-class
     {:component-did-mount #(rerender-cursor
                             @!ref num-divisions cursor-width)
      :component-did-update #(rerender-cursor
                              @!ref num-divisions cursor-width)
      :display-name "cursor"
      :reagent-render (fn [num-divisions]
                        @(subscribe [:viewport-width])
                        @(subscribe [:numerator-divisions])
                        @(subscribe [:denominator-divisions])
                        [:hr
                         (use-style
                          cursor-style
                          {:id    "cursor"
                           :width cursor-width
                           :size  "800"
                           :ref   #(reset! !ref %)})])})))

(defn- container-style
  [is-mobile?]
  (let [k              (if is-mobile? :mobile :desktop)
        margin-extra   (if is-mobile? 16 32)
        margin-top     (-> navbar-height k (+ margin-extra) (str "px"))
        padding-bottom (if is-mobile? "0" "5rem")
        height         (str "calc(100vh - " margin-top " - " padding-bottom ")")
        position       (if is-mobile? "relative" "unset")]
    {:margin-top       margin-top
     :padding-bottom   padding-bottom
     :background-color "#fafafa"
     :box-shadow       "2px 2px 5px rgba(0,0,0,0.6)"
     :border-radius    "5px"
     :font-family      "lato-light, sans-serif"
     :box-sizing       "border-box"
     :height           height
     :max-height       "800px"
     :width            "100%"
     :display          "flex"
     :min-height       "0"
     :flex-direction   "column"
     :position         position}))

(defn- metronome-group-style
  [is-mobile?]
  {:padding         (if is-mobile? "0.5rem 0" "0 3rem")
   :display         "flex"
   :flex-direction  "column"
   :overflow        "hidden"
   :flex            "1 1 auto"
   :justify-content "center"})

(stylefy/tag "body"
             {:overflow-y "auto !important"
              :padding-right "0 !important"})

(defn polyrhythm-container []
  (let [numerator       @(subscribe [:numerator-divisions])
        denominator     @(subscribe [:denominator-divisions])
        total-divisions @(subscribe [:lcm])
        is-mobile?      @(subscribe [:is-mobile?])
        is-portrait?    @(subscribe [:is-portrait?])]
    [:<>
     [mui/dialog {:open is-portrait?}
      [mui/dialog-title "Rotate to Landscape"]
      [mui/dialog-content
       [mui/dialog-content-text "The polyrhythm metronome layout works best in landscape mode."]]]
     [mui/paper (use-style (container-style @(subscribe [:is-mobile?]))
                           {:elevation 3})
      [settings-container]
      [:div (use-style (metronome-group-style is-mobile?))
       [cursor]
       [control-group numerator denominator total-divisions is-mobile?]
       [visualizer-grid]
       [tempo-play-group]]]]))