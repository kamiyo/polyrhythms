(ns app.db
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]
            [app.polyrhythms.common :refer [lcm]]
            [app.routes :refer [routes]]))

(s/def ::is-verbose? boolean?)
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
(s/def ::route (set routes))

(s/def ::db
  (s/keys :req-un
          [::is-mobile?
           ::route
           ::numerator
           ::denominator
           ::lcm
           ::tempo
           ::last-beat-time
           ::is-playing?
           ::is-verbose?]))

(def default-db
  {:is-mobile? false
   :route :polyrhythms
   :numerator {:divisions 3
               :microbeat 0}
   :denominator {:divisions 2
                 :microbeat 0}
   :lcm (lcm 3 2)
   :last-beat-time 0
   :tempo 60
   :is-playing? false
   :is-verbose? false})