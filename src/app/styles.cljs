(ns app.styles
  (:require [stylefy.core :refer [font-face]]))

(defonce navbar-height 80)

(font-face {:font-family "lato-light"
            :src "url('/fonts/lato-light.woff2') format('woff2')"
            :font-weight "normal"
            :font-style "normal"})