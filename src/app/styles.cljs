(ns app.styles
  (:require [stylefy.core :refer [font-face]]))

(defonce navbar-height {:mobile  60
                        :desktop 80})

(defonce light-blue "rgb(78, 134, 164)")
(defonce light-blue-transparent "rgb(78, 134, 164, 0.08)")
(defonce dark-blue "rgb(10, 66, 96)")

(font-face {:font-family "lato-light"
            :src "url('/fonts/lato-light.woff2') format('woff2')"
            :font-weight "normal"
            :font-style "normal"})