(ns app.subs
 (:require [re-frame.core :refer [reg-sub subscribe]]))

(reg-sub
 :numerator-divisions
 (fn [db _]
   (get-in db [:numerator :divisions])))

(reg-sub
 :denominator-divisions
 (fn [db _]
   (get-in db [:denominator :divisions])))

(reg-sub
 :tempo
 (fn [db _]
   (:tempo db)))

(reg-sub
 :is-playing?
 (fn [db _]
   (:is-playing? db)))

(reg-sub
 :last-beat-time
 (fn [db _]
   (:last-beat-time db)))

(reg-sub
 :numerator-microbeat
 (fn [db _]
   (get-in db [:numerator :microbeat])))

(reg-sub
 :denominator-microbeat
 (fn [db _]
   (get-in db [:denominator :microbeat])))

(reg-sub
 :route
 (fn [db _]
   (:route db)))

(reg-sub
 :display-type
 (fn [db _]
   (:display-type db)))