(ns app.db
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))

(s/def ::tempo number?)
(s/def ::is-playing? boolean?)
(s/def ::next-note-time number?)
(s/def ::subdivision
  (s/keys :req-un
          [::divisions
           ::next-note-time]))

(s/def ::numerator ::subdivision)
(s/def ::denominator ::subdivision)

(s/def ::db
  (s/keys :req-un
          [::numerator
           ::denominator
           ::tempo
           ::is-playing?]))

(def default-db
  {:numerator {:divisions 3
               :next-note-time 0}
   :denominator {:divisions 2
                 :next-note-time 0}
   :tempo 60
   :is-playing? false})