(ns app.styles
  (:require [stylefy.core :refer [font-face]]))

(defonce navbar-height {:mobile  50
                        :desktop 80})

(defn get-navbar-height [is-mobile?]
  ((if is-mobile? :mobile :desktop) navbar-height))

(defonce light-blue "rgb(78, 134, 164)")
(defonce light-blue-transparent "rgb(78, 134, 164, 0.08)")
(defonce dark-blue "rgb(10, 66, 96)")

(font-face {:font-family "lato-light"
            :src "url('/fonts/lato-light.woff2') format('woff2')"
            :font-weight "normal"
            :font-style "normal"})