(ns app.routes
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [accountant.core :as accountant]
            [re-frame.core :refer [dispatch]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]))

(def routes #{:polyrhythms})

(defn init-router! []
  (accountant/configure-navigation!
   {:nav-handler (fn [path]
                   (secretary/dispatch! path))
    :path-exists? (fn [path]
                    (secretary/locate-route path))}))

(defn init-app-routes []
  (defroute "/" []
    (dispatch [:change-route :polyrhythms]))
  (defroute "/polyrhythms" []
    (dispatch [:change-route :polyrhythms]))
  (init-router!))