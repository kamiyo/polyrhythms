(ns app.core
  (:require [re-frame.core :refer [dispatch-sync dispatch]]
            [reagent.core :as r]
            [app.polyrhythms.common :refer [worker init-audio]]
            [app.subs]
            [app.routes :refer [init-app-routes]]
            [app.styles]
            [app.nav]
            [app.events]
            [app.views :refer [app]]
            [app.polyrhythms.sound :refer [lookahead scheduler]]
            [app.polyrhythms.animation]
            [stylefy.core :as stylefy]
            ["mobile-detect" :as mobile-detect]))

(defn listen-worker
  [^js e]
  (when (= (.-data e) "tick")
    (scheduler)))

(defn listen-browser
  [^js e]
  (let [mobile? (-> (mobile-detect. js/window.navigator.userAgent) .mobile some?)
        innerWidth (.-innerWidth js/window)
        innerHeight (.-innerHeight js/window)
        ratio (/ innerWidth innerHeight)]
    (dispatch [:update-layout
               {:is-mobile? mobile?
                :width innerWidth
                :height innerHeight
                :is-portrait? (<= ratio 1)}])))

(defn start []
  (init-audio)
  (init-app-routes)
  (.addEventListener ^js @worker "message" listen-worker)
  (.addEventListener ^js js/window "resize" listen-browser)
  (.postMessage ^js @worker (clj->js {:interval lookahead}))
  (listen-browser nil)
  (r/render [app] (.getElementById js/document "app")))

(stylefy/init
 {:global-vendor-prefixed
  {::stylefy/vendors      ["webkit" "moz" "o"]
   ::stylefy/auto-prefix #{:border-radius}}})

(dispatch-sync [:initialise-db])

(defn stop []
  (.postMessage @worker "stop")
  (.removeEventListener js/window "resize" listen-browser)
  (.removeEventListener @worker "tick" listen-worker))

(defn ^:export main []
  (start))