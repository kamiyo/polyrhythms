(ns app.db
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [app.routes :refer [routes]]))


(s/def ::is-mobile? boolean?)
(s/def ::tempo number?)
(s/def ::is-playing? boolean?)
(s/def ::last-beat-time number?)
(s/def ::divisions number?)
(s/def ::microbeat number?)
(s/def ::subdivision
  (s/keys :req-un
          [::divisions
           ::microbeat]))

(s/def ::numerator ::subdivision)
(s/def ::denominator ::subdivision)
(s/def ::route routes)

(s/def ::db
  (s/keys :req-un
          [::is-mobile?
           ::route
           ::numerator
           ::denominator
           ::tempo
           ::last-beat-time
           ::is-playing?]))

(def default-db
  {:is-mobile? false
   :route :polyrhythms
   :numerator {:divisions 3
               :microbeat 0}
   :denominator {:divisions 2
                 :microbeat 0}
   :last-beat-time 0
   :tempo 60
   :is-playing? false})