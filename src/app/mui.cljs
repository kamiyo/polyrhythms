(ns app.mui
  (:require [reagent.core :as r]
            ["@material-ui/core/Textfield" :default TextField]
            ["@material-ui/core/Menu" :default Menu]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/FormGroup" :default FormGroup]
            ["@material-ui/core/FormControlLabel" :default FormControlLabel]
            ["@material-ui/core/Switch" :default Switch]
            ["@material-ui/core/Paper" :default Paper]))

(def text-field         (r/adapt-react-class TextField))
(def menu               (r/adapt-react-class Menu))
(def menu-item          (r/adapt-react-class MenuItem))
(def form-group         (r/adapt-react-class FormGroup))
(def form-control-label (r/adapt-react-class FormControlLabel))
(def switch             (r/adapt-react-class Switch))
(def paper              (r/adapt-react-class Paper))