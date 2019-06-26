(ns app.polyrhythms.tempo
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [stylefy.core :as stylefy :refer [use-style]]
            [app.mui :as mui]
            [app.polyrhythms.buttons :refer [play-button pause-button]]
            [app.polyrhythms.sound :refer [play]]
            [app.polyrhythms.styles :refer [mui-override-style]]
            [app.polyrhythms.common :refer [context]]))

(defonce touch-init (r/atom 0))
(defonce tempo-on-touch (r/atom 0))
(defonce slider-angle (r/atom 0))
(defonce slider-height 50)
(defonce slider-width 300)

(defn update-canvas []
  (let [angle @slider-angle
        canvas (js/document.getElementById "slider-canvas")
        context (.getContext canvas "2d")
        scale (/ js/Math.PI 3)]
    (.clearRect context 0 0 (.-width canvas) (.-height canvas))
    (dorun
     (for [arr (range -5 6 0.5)
           :let [val (-> arr (* 2) (+ angle) (/ 10) (* scale))
                 left (-> arr
                          (- 0.05)
                          (* 2)
                          (+ angle) ; [0 1)
                          (/ 10) ; results in [-1 1]
                          (* scale) ; results in [-pi/3 pi/3]
                          (js/Math.sin) ; results in [-sqrt(3)/2 sqrt(3)/2]
                          (/ (/ (js/Math.sqrt 3) 2))) ; results in [-1 1]
                 right (-> arr
                           (+ 0.05)
                           (* 2)
                           (+ angle)
                           (/ 10)
                           (* scale)
                           (js/Math.sin)
                           (/ (/ (js/Math.sqrt 3) 2)))
                 width (* 100 (- right left))
                 pos (-> val
                         (js/Math.sin)
                         (/ (/ (js/Math.sqrt 3) 2))
                         (* (/ slider-width 2))
                         (+ (/ slider-width 2)))]]
       (do
         (set! (.-strokeStyle context) (str "rgba(0, 0, 0, " (- 1 (js/Math.abs (js/Math.sin val))) ")"))
         (set! (.-lineWidth context) width)
         (.beginPath context)
         (.moveTo context pos 0)
         (.lineTo context pos slider-height)
         (.stroke context))))))

(defn on-touch-move [e]
  (let [touch (.. e -touches (item 0) -clientX)
        diff (- touch @touch-init)]
    (reset! slider-angle (mod (/ diff 20) 1))
    (dispatch [:change-tempo (+ @tempo-on-touch (js/Math.round (/ diff 20)))])))

(def canvas-style
  {:margin "0 2rem"})

(defn slider-canvas []
  (r/create-class
   {:component-did-mount #(update-canvas)
    :component-did-update #(update-canvas)
    :display-name "slider-canvas"
    :reagent-render (fn []
                      @slider-angle
                      [:canvas
                       (use-style
                        canvas-style
                        {:id "slider-canvas"
                         :width slider-width
                         :height slider-height
                         :on-touch-start (fn [e]
                                           (let [touch (.. e -touches (item 0) -clientX)]
                                             (reset! touch-init touch)
                                             (reset! tempo-on-touch @(subscribe [:tempo]))))
                         :on-touch-move on-touch-move})])}))

(defn- tempo-field-style [is-mobile?]
  {:padding (if is-mobile? "0" "2rem 0")
   ::stylefy/manual (mui-override-style false (if is-mobile? "2.2rem" "4rem"))})

(def tempo-input-style
  {:margin "0 1rem"})

(defn tempo-control [is-mobile?]
  (fn [is-mobile?]
    [:div
     (use-style (tempo-field-style is-mobile?))
     [mui/text-field
      (use-style
       tempo-input-style
       {:key       "tempo"
        :type      "number"
        :variant   "outlined"
        :margin    "dense"
        :label     "tempo"
        :name      "tempo"
        :disabled  is-mobile?
        :value     (str @(subscribe [:tempo]))
        :onWheel  #(let [del    (.-deltaY %)
                         tempo @(subscribe [:tempo])]
                     (cond
                       (pos? del) (dispatch [:change-tempo (dec tempo)])
                       (neg? del) (dispatch [:change-tempo (inc tempo)])))
        :onChange #(dispatch [:change-tempo (.. % -target -value)])
        :onBlur   #(dispatch [:change-tempo (.. % -target -value)])})]]))

(defn tempo-play-style [is-mobile?]
  {:text-align "center"
   :display "flex"
   :flex-direction "row-reverse"
   :flex "0 0 auto"
   :justify-content "center"
   :align-items "center"})

(defn handle-play-click
  [event]
  (if (= (.-state context) "suspended")
    (-> (.resume context) (.then (play)))
    (play)))

(defn tempo-play-group []
  (let [is-mobile?    @(subscribe [:is-mobile?])
        is-playing?   @(subscribe [:is-playing?])
        button-height (if is-mobile? 60 120)
        button-width  (if is-mobile? 95 120)]
    [:div
     (use-style (tempo-play-style is-mobile?))
     [tempo-control is-mobile?]
     (when is-mobile? [slider-canvas])
     (if is-playing?
       [pause-button {:on-click handle-play-click
                      :width button-width
                      :height button-height}]
       [play-button  {:on-click handle-play-click
                      :width button-width
                      :height button-height}])]))