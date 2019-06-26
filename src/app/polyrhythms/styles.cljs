(ns app.polyrhythms.styles
  (:require [app.styles :refer [light-blue]]))

(defn mui-override-style
  ([is-mobile? width]
   [[:label.Mui-focused {:color light-blue}]
    [:.MuiOutlinedInput-root.Mui-focused
     [:.MuiOutlinedInput-notchedOutline {:border-color light-blue
                                         :color light-blue}]]
    [:.MuiOutlinedInput-inputMarginDense (merge
                                          (if (some? width) {:width width}))]])
  ([is-mobile?] (mui-override-style is-mobile? nil)))