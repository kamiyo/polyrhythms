(ns app.core
  (:require [re-frame.core :refer [dispatch-sync]]
            [reagent.core :as r]
            [app.common :refer [worker]]
            [app.events]
            [app.views]
            [app.subs]
            [app.sound]
            [app.animation]
            [stylefy.core :as stylefy]))

(defn start
  []
  (app.common/init-audio)
  (.addEventListener
   @worker
   "message"
   (fn [e]
     (when (= (.-data e) "tick")
       (app.sound/scheduler))))
  (.postMessage @worker (clj->js {:interval app.sound/lookahead}))
  (r/render-component [app.views/app]
                      (.getElementById js/document "app")))

(stylefy/init {:global-vendor-prefixed {::stylefy/vendors ["webkit" "moz" "o"]
                                        ::stylefy/auto-prefix #{:border-radius}}})

(dispatch-sync [:initialise-db])

(defn stop
  []
  (.postMessage @worker "stop"))

(defn ^:export main
  []
  (start))