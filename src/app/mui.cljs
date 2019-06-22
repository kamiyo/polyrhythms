(ns app.mui
  (:require [reagent.core :as r]
            ["@material-ui/core/AppBar" :default AppBar]
            ["@material-ui/core/Textfield" :default TextField]
            ["@material-ui/core/Dialog" :default Dialog]
            ["@material-ui/core/DialogTitle" :default DialogTitle]
            ["@material-ui/core/DialogContent" :default DialogContent]
            ["@material-ui/core/DialogContentText" :default DialogContentText]
            ["@material-ui/core/Menu" :default Menu]
            ["@material-ui/core/MenuItem" :default MenuItem]
            ["@material-ui/core/FormGroup" :default FormGroup]
            ["@material-ui/core/FormControlLabel" :default FormControlLabel]
            ["@material-ui/core/Drawer" :default Drawer]
            ["@material-ui/core/List" :default List]
            ["@material-ui/core/ListItem" :default ListItem]
            ["@material-ui/core/ListItemText" :default ListItemText]
            ["@material-ui/core/IconButton" :default IconButton]
            ["@material-ui/icons/Menu" :default MenuIcon]
            ["@material-ui/core/Switch" :default Switch]
            ["@material-ui/core/Paper" :default Paper]))

(def app-bar             (r/adapt-react-class AppBar))
(def dialog              (r/adapt-react-class Dialog))
(def dialog-title        (r/adapt-react-class DialogTitle))
(def dialog-content      (r/adapt-react-class DialogContent))
(def dialog-content-text (r/adapt-react-class DialogContentText))
(def text-field          (r/adapt-react-class TextField))
(def menu                (r/adapt-react-class Menu))
(def menu-item           (r/adapt-react-class MenuItem))
(def form-group          (r/adapt-react-class FormGroup))
(def form-control-label  (r/adapt-react-class FormControlLabel))
(def switch              (r/adapt-react-class Switch))
(def paper               (r/adapt-react-class Paper))
(def menu-icon           (r/adapt-react-class MenuIcon))
(def icon-button         (r/adapt-react-class IconButton))
(def drawer              (r/adapt-react-class Drawer))
(def mlist               (r/adapt-react-class List))
(def mlist-item          (r/adapt-react-class ListItem))
(def mlist-item-text     (r/adapt-react-class ListItemText))