(ns app.polyrhythms.settings
  (:require [reagent.core :as r]
            [stylefy.core :as stylefy :refer [use-style]]
            [re-frame.core :refer [dispatch subscribe]]
            ["@material-ui/core/Switch" :default Switch]
            [app.svgs.gear :refer [gear-svg]]
            [app.mui :as mui]
            [app.styles :refer [light-blue light-blue-transparent dark-blue]]))

(def settings-container-style
  {:display         "flex"
   :justify-content "flex-end"
   :margin-bottom   "3rem"})

(defn- get-gear-style
  [is-open?]
  {:margin "0.5rem"
   :height "1.5rem"
   :width "1.5rem"
   :transition "all 0.8s"
   :transform (if is-open? "rotate(360deg)" "rotate(0)")
   ::stylefy/mode
   {:hover {:cursor "pointer"}}})

(def mui-switch-override-style
  {::stylefy/manual [[:.MuiSwitch-colorSecondary.Mui-checked
                      [[:& {:color light-blue}]
                       [:&:hover {:background-color light-blue-transparent}]]]
                     [".MuiSwitch-colorSecondary.Mui-checked + .MuiSwitch-track" {:background-color light-blue}]]})

(defn verbose-toggler []
  [mui/menu-item
   [mui/form-group
    {:row true}
    [mui/form-control-label
     (use-style mui-switch-override-style
                {:control (r/create-element
                           Switch
                           (clj->js
                            {:checked @(subscribe [:is-verbose?])
                             :onChange #(dispatch [:toggle-is-verbose?])
                             :value "is-verbose?"}))
                 :label "Verbose UI"})]]])

(defn settings-container []
  (let [state         (r/atom {:anchor-el nil})
        handle-open  #(swap! state assoc :anchor-el (.-currentTarget %))
        handle-close #(swap! state assoc :anchor-el nil)]
    (fn []
      (let [is-open? (-> @state :anchor-el some?)]
        [:div (use-style settings-container-style)
         [gear-svg (use-style
                (get-gear-style is-open?)
                {:on-click handle-open})]

         [mui/menu
          {:open                  is-open?
           :anchor-el             (:anchor-el @state)
           :get-content-anchor-el nil
           :keep-mounted          true
           :anchor-origin         #js {:vertical "bottom"
                                       :horizontal "right"}
           :transform-origin      #js {:vertical "top"
                                       :horizontal "right"}
           :on-close              handle-close}
          (verbose-toggler)]]))))