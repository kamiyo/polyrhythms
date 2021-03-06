(ns app.events
  (:require [re-frame.core :refer [reg-event-db reg-event-fx reg-fx after debug]]
            [app.db :refer [default-db]]
            [cljs-bach.synthesis :as a]
            [app.polyrhythms.common :refer [get-context-current-time get-seconds-per-beat lcm]]
            [cljs.spec.alpha :as s]))

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

;; now we create an interceptor using `after`
(def check-spec-interceptor (after (partial check-and-throw :app.db/db)))

(def debug? ^boolean goog.DEBUG)
(def standard-interceptors [(when debug? debug)
                            (when debug? check-spec-interceptor)])

(reg-event-db
 :initialise-db
 (fn [_ _]
   default-db))

(reg-event-fx
 :change-divisions
 [standard-interceptors]
 (fn [cofx [_ new-values]]
   (let [{:keys [divisions which]} new-values
         parsed-int                (max 1 (js/parseInt divisions))
         db                        (:db cofx)
         temp                      (assoc
                                    {:numerator (-> db :numerator :divisions)
                                     :denominator (-> db :denominator :divisions)}
                                    which parsed-int)
         lcm (lcm (:numerator temp) (:denominator temp))]
     {:db       (-> (:db cofx)
                    (assoc-in [which :divisions] parsed-int)
                    (assoc-in [:lcm] lcm))
      :dispatch [:change-last-beat-time (get-context-current-time)]})))

(reg-event-db
 :inc-microbeat
 [standard-interceptors]
 (fn [db [_ which]]
   (update-in db [which :microbeat] inc)))

(reg-event-db
 :reset-microbeats
 [standard-interceptors]
 (fn [db [_ _]]
   (-> db
       (assoc-in [:numerator   :microbeat] 0)
       (assoc-in [:denominator :microbeat] 0))))

(reg-event-db
 :normalize-microbeats
 [standard-interceptors]
 (fn [db [_ _]]
   (let [{num-microbeat :microbeat
          num-divisions :divisions} (:numerator db)
         {den-microbeat :microbeat
          den-divisions :divisions} (:denominator db)]
     (-> db
         (assoc-in [:numerator   :microbeat] (mod num-microbeat num-divisions))
         (assoc-in [:denominator :microbeat] (mod den-microbeat den-divisions))
         (update-in [:last-beat-time] + (get-seconds-per-beat (:tempo db)))))))

(reg-event-db
 :change-last-beat-time
 [check-spec-interceptor]
 (fn [db [_ new-value]]
   (assoc db :last-beat-time new-value)))

(reg-event-fx
 :check-diff
 [check-spec-interceptor]
 (fn [cofx [_ args]]
   (if (> args 1)
     {:dispatch [:change-last-beat-time (get-context-current-time)]})))

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
   (update-in db [:is-playing?] not)))

(reg-event-db
 :change-route
 [check-spec-interceptor]
 (fn [db [_ route]]
   (assoc db :route route)))

(reg-event-db
 :update-layout
 [check-spec-interceptor]
 (fn [db [_ values-map]]
   (update db :layout merge values-map)))

(reg-event-db
 :toggle-is-verbose?
 [check-spec-interceptor]
 (fn [db [_ _]]
   (update-in db [:is-verbose?] not)))
