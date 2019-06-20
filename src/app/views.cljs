(ns app.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [app.polyrhythms.views :refer [polyrhythm-container]]
            ["@material-ui/styles" :refer [StylesProvider]]))

(defmulti current-page (fn [_] @(subscribe [:route])))
(defmethod current-page :polyrhythms []
  [polyrhythm-container])
(defmethod current-page :default []
  [polyrhythm-container])

(defn app
  []
  [:> StylesProvider {:inject-first true}
   [app.nav/logo-svg]
   [:div
    [app.nav/navbar]
    [current-page]]])