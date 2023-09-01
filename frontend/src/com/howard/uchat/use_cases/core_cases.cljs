(ns com.howard.uchat.use-cases.core-cases
  (:require
   [re-frame.core :as re-frame]
   [com.howard.uchat.db :as db]
   [com.howard.uchat.api :as api]
   [com.howard.uchat.db-spec :as spec]
   [cljs.spec.alpha :as s]
   [tools.reframetools :refer [sdb gdb]]))
   ;[Day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

;; TODO: to mono repo.
(defn get-error-field
  "get error field from spec and data (which must be a map),
  return a vector contain failed key"
  [s data]
  (let [test (s/explain-data s data)]
    (->> (::s/problems test)
         (map :in)
         (map #(nth % 0))
         (map keyword)
         (into []))))

(re-frame/reg-sub ::name (gdb [:name]))
(re-frame/reg-sub ::re-pressed-example  (gdb [:re-pressed-example]))

(re-frame/reg-event-db ::initialize-db (constantly db/default-db))

(re-frame/reg-event-db ::register-request-result
                       (fn [db [_ status response]]
                         (assoc db :register-request
                                {:status status
                                 :response response})))

(re-frame/reg-fx ::register-request
                 (fn [reg-form]
                   (api/register
                    reg-form
                    #(re-frame/dispatch [::register-request-result :success %])
                    #(re-frame/dispatch [::register-request-result :fail %]))))

(re-frame/reg-event-db
 ::clear-register-validate
 (fn [db [& _]]
   (assoc db :register-validate [])))

(re-frame/reg-event-fx
 ::register
 (fn [{:keys [db]} [_ reg-form]]
   (let [validate (get-error-field spec/post-user-spec reg-form)
         {password :password
          confirm :confirm-password} reg-form
         error-fields (->> (conj validate
                                 (when-not (= password confirm) :confirm-password))
                           (filter #(not (nil? %))))]
     (if (> (count error-fields) 0)
       {:db (assoc db :register-validate error-fields)}
       {:dispatch [::register-request-result :request]
        ::register-request reg-form}))))

(comment
  (def login-test (s/explain-data spec/post-login-spec {:username "hello" :password 1}))

  (->> (::s/problems login-test)
       (map :in)
       (map #(first %))
       (into []))
  (def register-test
    (s/explain-data spec/post-user-spec {:username "hello" :password "1234"}))
  (s/explain-str spec/post-user-spec {:username "hello" :password "1234"})
  (js/console.log "hello, world"))
