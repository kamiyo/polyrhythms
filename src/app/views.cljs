(ns app.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [app.polyrhythms.views :refer [polyrhythm-container]]))

(defmulti current-page (fn [_] @(subscribe [:route])))
(defmethod current-page :polyrhythms []
  [polyrhythm-container])
(defmethod current-page :default []
  [polyrhythm-container])

(defn app
  []
  [:<>
   [app.nav/logo-svg]
   [:div
    [app.nav/navbar]
    [current-page]]])