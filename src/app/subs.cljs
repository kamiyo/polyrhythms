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
 :numerator-next-note-time
 (fn [db _]
   (get-in db [:numerator :next-note-time])))

(reg-sub
 :denominator-next-note-time
 (fn [db _]
   (get-in db [:denominator :next-note-time])))