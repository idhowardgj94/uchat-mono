(ns com.howard.uchat.use-cases.core-cases
  (:require
   [re-frame.core :as re-frame]
   [com.howard.uchat.db :as db]
   [com.howard.uchat.api :as api]
   [com.howard.uchat.db-spec :as spec]
   [cljs.spec.alpha :as s]
   [com.howard.uchat.use-cases.socket :as socket]
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
(print "he")
(re-frame/reg-event-db ::initialize-db (fn-traced [db [_ debug-tools]]
                                                  (assoc db/default-db
                                                         :debug-tools debug-tools)))

(re-frame/reg-event-db ::register-result
                       (fn-traced [db [_ status response]]
                                  (assoc db :register-request
                                         {:status status
                                          :response response})))

(re-frame/reg-fx
 ::get-me
 (fn-traced
  [_]
  (-> (api/get-me)
      (.then (fn [response]
               (let [data (:data response)]
                 (re-frame/dispatch [::assoc-db :user data])
                 ))))))

(re-frame/reg-event-fx
 ::store-token-and-login
 (fn-traced
  [{:keys [db]} [_ response]]
  (let [{token :token} response]
    (.setItem js/localStorage "token" token)
    (reset! api/token token)
    (api/add-axios-auth token)
    (socket/init-sente token)
    (socket/start-router!)
    {:db (assoc db
                :token token
                :auth? true)
     :fx [[::get-me]]})))

;; TODO: migrate to axios, and remove useless request result event
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
                {::register-request reg-form}))))

;; ------------ login ------------------
(re-frame/reg-fx
 ::login-request
 (fn-traced
  [form]
  (-> (api/login form)
      (.then (fn [res]
               (let [data (:data res)]
                 (re-frame/dispatch-sync [::store-token-and-login data]))))
      (.catch (fn [err]
                (let [data err]
                  (re-frame/dispatch [::assoc-db :login-error-message (.-response data)])))))))

(re-frame/reg-event-db
 ::clear-login-validate
 (fn-traced [db [& _]]
            (assoc db
                   :login-validate []
                   :login-error-message nil)))

(re-frame/reg-event-fx
 ::login
 (fn-traced
  [{:keys [db]} [_ form]]
  (let [error-fields (get-error-fields spec/login-form-spec form)]
    (if (> (count error-fields) 0)
      {:db (assoc db :login-validate error-fields)}
      {::login-request form}))))

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
 ::check-user-context-success
 (fn-traced
  [db _]
  (let [{:keys [direct-subscriptions channel-subscriptions]} db]
    (if (and (some? direct-subscriptions)
             (some? channel-subscriptions))
      (assoc db :user-context-status :success)
      db))))

(re-frame/reg-event-db
 ::assoc-db
 (fn-traced
  [db [_ key value]]
  (assoc db key value)))

(re-frame/reg-event-db
 ::assoc-in-db
 (fn-traced [db [_ keys value]]
            (assoc-in db keys value)))

(re-frame/reg-event-fx
 ::get-subscriptions
 (fn-traced
  [{:keys [db]} [_ type]]
  ;; TODO: team
  (let [team_uuid (-> db
                      :current-team
                      :team_uuid)]
    (api/get-subscription
     {:type type
      :team_uuid team_uuid}
     #(case type
        "direct" (do 
                     (re-frame/dispatch [::assoc-db :direct-subscriptions (:result %)])
                     (re-frame/dispatch [::check-user-context-success]))
        "channel" (do (re-frame/dispatch [::assoc-db :channel-subscriptions (:result %)])
                      (re-frame/dispatch [::check-user-context-success]))))
    nil)))

(re-frame/reg-fx
 ::get-teams
 (fn-traced
  [_]
  (api/get-teams #(do
                    (re-frame/dispatch [::assoc-db :teams (:result %)])
                    (re-frame/dispatch [::assoc-db :current-team (first (:result %))])
                    (re-frame/dispatch [::get-subscriptions "direct"])
                    (re-frame/dispatch [::get-subscriptions "channel"])))))

(re-frame/reg-event-fx
 ::prepare-user-context
 (fn-traced
  [{:keys [db]} _]
  {:fx [[:dispatch [::assoc-db :user-context-status :request]]
        [::get-teams nil]]
   :db (assoc db
              :user-context-status :request
              :direct-subscriptions nil
              :channel-subscriptions nil
              :teams nil
              :current-team nil)}))



;; ---- generate direct channel
(re-frame/reg-event-db
 ::set-direct-uuid
 (fn-traced
  [db [_ other-user channel-uuid]]
  (let [directs (-> db :direct-subscriptions)]
    (assoc db :direct-subscriptions
           (->> directs
                (map #(if (= (:other_user %) other-user)
                        (assoc % :channel_uuid channel-uuid)
                        %)))))))

(re-frame/reg-event-fx
 ::generate-direct-channel
 (fn-traced
  [{:keys [db]} [_ other-user]]
  (let [team-uuid (-> db :current-team :team_uuid)]
    (-> (api/post-generate-direct team-uuid other-user)
        (.then #(do
                  (let [channel-uuid (-> % :data :result)]
                    (re-frame/dispatch [::set-direct-uuid other-user channel-uuid])
                    (re-frame/dispatch [:routes/navigate [:routes/channels {:uuid channel-uuid
                                                                            :channel-type :direct}]]))))))))

(re-frame/reg-event-fx
 :db/assoc
 (fn-traced
  [db [_  k v]]
   (assoc db k v)))
