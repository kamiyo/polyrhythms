(ns app.core
  (:require [re-frame.core :refer [dispatch-sync dispatch]]
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
            [stylefy.core :as stylefy]
            ["mobile-detect" :as mobile-detect]))

(defn listen-worker [^js e]
  (when (= (.-data e) "tick")
    (app.polyrhythms.sound/scheduler)))

(defn listen-browser [^js e]
  (let [mobile? (-> (mobile-detect. js/window.navigator.userAgent) .mobile some?)]
    (js/console.log "updating" mobile?)
    (dispatch [:update-is-mobile? mobile?])))

(defn start
  []
  (app.common/init-audio)
  (app.routes/init-app-routes)
  (.addEventListener ^js @worker "message" listen-worker)
  (.addEventListener ^js js/window "resize" listen-browser)
  (.postMessage ^js @worker (clj->js {:interval app.polyrhythms.sound/lookahead}))
  (r/render [app.views/app]
            (.getElementById js/document "app")))

(stylefy/init {:global-vendor-prefixed {::stylefy/vendors ["webkit" "moz" "o"]
                                        ::stylefy/auto-prefix #{:border-radius}}})

(dispatch-sync [:initialise-db])

(defn stop
  []
  (.postMessage @worker "stop")
  (.removeEventListener js/window "resize" listen-browser)
  (.removeEventListener @worker "tick" listen-worker))

(defn ^:export main
  []
  (start))