(ns app.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx after]]
            [app.db :refer [default-db]]
            [cljs-bach.synthesis :as a]
            [cljs.spec.alpha :as s]))

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw :app.db/db)))

(reg-event-db
 :initialise-db
 (fn [_ _]
   default-db))

(reg-event-fx
 :change-divisions
 [check-spec-interceptor]
 (fn [cofx [_ new-values]]
   (let [{:keys [divisions which]} new-values]
     {:db (assoc-in (:db cofx) [which :divisions] (max 1 (js/parseInt divisions)))
      :dispatch [:change-all-next-note-times (a/current-time app.sound/context)]})))

(reg-event-db
 :change-next-note-time
 [check-spec-interceptor]
 (fn [db [_ new-values]]
   (let [{:keys [next-note-time which]} new-values]
     (assoc-in db [which :next-note-time] next-note-time))))

(reg-event-db
 :change-all-next-note-times
 [check-spec-interceptor]
 (fn [db [_ value]]
   (js/console.log value)
   (-> db
       (assoc-in [:numerator :next-note-time] value)
       (assoc-in [:denominator :next-note-time] value))))

(reg-fx
 :play-once
 (fn [_]
   (app.sound/play-once (a/current-time app.sound/context) 1980)
   (app.sound/play-once (a/current-time app.sound/context) 1320)))

(reg-event-fx
 :check-diff
 [check-spec-interceptor]
 (fn [cofx [_ args]]
   (if (> args 1)
     {:dispatch [:change-all-next-note-times (a/current-time app.sound/context)]
      :play-once nil})))

(reg-event-fx
 :change-tempo
 [check-spec-interceptor]
 (fn [cofx [_ new-value]]
   (let [old-num (-> cofx :db :tempo)
         new-num (js/parseFloat new-value)
         new-num-adjusted (if (<= new-num 0) old-num new-num)]
     {:db (assoc (:db cofx) :tempo new-num-adjusted)
      :dispatch [:check-diff (Math/abs (- new-num-adjusted old-num))]})))

(reg-event-db
 :toggle-playing
 [check-spec-interceptor]
 (fn [db [_]]
   (assoc db :is-playing? (not (:is-playing? db)))))