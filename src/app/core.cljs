(ns app.core
  (:require [re-frame.core :refer [dispatch-sync]]
            [reagent.core :as r]
            [app.common :refer [worker]]
            [app.subs]
            [app.routes]
            [app.styles]
            [app.nav]
            [app.events]
            [app.views]
            [app.polyrhythms.sound]
            [app.animation]
            [stylefy.core :as stylefy]))

(defn listen-worker []
  (.addEventListener
   ^js @worker
   "message"
   (fn [^js e]
     (when (= (.-data e) "tick")
       (app.polyrhythms.sound/scheduler)))))

(defn start
  []
  (app.common/init-audio)
  (app.routes/init-app-routes)
  (listen-worker)
  (.postMessage ^js @worker (clj->js {:interval app.polyrhythms.sound/lookahead}))
  (r/render [app.views/app]
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