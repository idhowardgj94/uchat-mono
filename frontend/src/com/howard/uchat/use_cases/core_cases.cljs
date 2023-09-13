(ns com.howard.uchat.use-cases.core-cases
  (:require
   [re-frame.core :as re-frame]
   [com.howard.uchat.db :as db]
   [com.howard.uchat.api :as api]
   [com.howard.uchat.db-spec :as spec]
   [cljs.spec.alpha :as s]
   [spec-tools.data-spec :as ds]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   
   ))

(defn get-error-fields
  "get error field from spec and data (which must be a map),
  return a vector contain failed key"
  [s data]
  (let [test (s/explain-data s data)]
    (->> (::s/problems test)
         (map :in)
         (map #(nth % 0))
         (map keyword)
         (into []))))

(re-frame/reg-event-db ::initialize-db (constantly db/default-db))

(re-frame/reg-event-db ::register-result
                       (fn-traced [db [_ status response]]
                                  (assoc db :register-request
                                         {:status status
                                          :response response})))

(re-frame/reg-event-fx
 ::store-token-and-login
 (fn-traced
  [{:keys [db]} [_ response]]
  (let [{token :token} response]
    (js/console.log "token: " token)
    (.setItem js/localStorage "token" token)
    {:db (assoc db
                :token token
                :auth? true)})))

(re-frame/reg-fx ::register-request
                 (fn [reg-form]
                   (api/register
                    reg-form
                    #(do (re-frame/dispatch [::register-result :success %])
                         (re-frame/dispatch-sync [::store-token-and-login %])
                         (re-frame/dispatch [:routes/navigate :routes/channels-home]))
                    #(re-frame/dispatch [::register-result :fail %]))))

(re-frame/reg-event-db
 ::clear-register-validate
 (fn-traced [db [& _]]
            (assoc db :register-validate [])))

(re-frame/reg-event-fx
 ::register
 (fn-traced [{:keys [db]} [_ reg-form]]
            (let [validate (get-error-fields spec/post-user-spec reg-form)
                  {password :password
                   confirm :confirm-password} reg-form
                  error-fields (->> (conj validate
                                          (when-not (= password confirm) :confirm-password))
                                    (filter #(not (nil? %))))]
              (if (> (count error-fields) 0)
                {:db (assoc db :register-validate error-fields)}
                {:dispatch [::register-result :request]
                 ::register-request reg-form}))))

;; ------------ login ------------------
(re-frame/reg-event-db
 ::login-error-message
 (fn-traced
  [db [_ message]]
  (assoc db :login-error-message message)))

(re-frame/reg-fx
 ::login-request
 (fn-traced
  [form]
  (api/login form
             #(do (re-frame/dispatch [::login-result :success %])
                  (re-frame/dispatch-sync [::store-token-and-login %])
                  (re-frame/dispatch [:routes/navigate [:routes/channels-home]]))
             #(do (re-frame/dispatch [::login-result :fail %])
                  (println %)
                  (re-frame/dispatch [::login-error-message (:message (:response %))])))))
(re-frame/reg-event-db
 ::clear-login-validate
 (fn-traced [db [& _]]
            (assoc db
                   :login-validate []
                   :login-error-message nil)))

(re-frame/reg-event-db
 ::login-result
 (fn-traced
  [db [_ status response]]
  (assoc db :login-request {:status status
                            :response response})))

(re-frame/reg-event-fx
 ::login
 (fn-traced
  [{:keys [db]} [_ form]]
  (let [error-fields (get-error-fields spec/login-form-spec form)]
    (if (> (count error-fields) 0)
      {:db (assoc db :login-validate error-fields)}
      {:dispatch [::login-result :request]
       ::login-request form}))))

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

;; ------------------ get list

(re-frame/reg-event-db
 ::assoc-db
 (fn-traced
  [db [_ key value]]
  (js/console.log (clj->js value))
  (assoc db key value)))

(re-frame/reg-event-fx
 ::get-subscriptions
 (fn-traced
  [{:keys [db]} [_ type uuid]]
  ;; TODO: team
  (api/get-subscription
   {:type type
    :team_uuid uuid}
   #(case type
      "direct" (do (js/console.log "direct~~~" (get % "result"))
                   (re-frame/dispatch [::assoc-db :direct-subscriptions (:result %)]))
      "channel" (re-frame/dispatch [::assoc-db :channel-subscriptions (:result %)])))
  nil))


