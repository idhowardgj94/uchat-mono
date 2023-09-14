(ns com.howard.uchat.backend.subscriptions.core
  (:require
   [spec-tools.data-spec :as ds]
   [clojure.spec.alpha :as s]
   [com.howard.uchat.backend.subscriptions.database :as db]  
   [com.howard.uchat.backend.teams.interface :as teams]
   
   [com.howard.uchat.backend.database.interface :as database]))

#_(ns-unalias *ns* 's)

(defonce success "success")
(defonce no-permission "no-permission")

(def user-team-subscriptions-params-spec
  (ds/spec {:name ::user-team-subscriptions-params
            :spec {:type (s/spec #{:direct :channel})
                   :username string?
                   :team-uuid uuid?}}))

(defn get-user-team-subscirptions
  "
  TODO: spec
  get user's subscription
  recived a map and return result
  opt req:
  - type: :direct or :channel
  - username
  - team-uuid"
  [db-conn opt]
  {:pre [(s/valid? user-team-subscriptions-params-spec opt)]}
  (let [{:keys [type username team-uuid]} opt]
    (if (false? (teams/is-user-in-team username team-uuid))
      {:status no-permission}
      {:status success :result (case type
                                 :direct (db/get-user-team-direct-subscriptions db-conn username team-uuid)
                                 :channel (db/get-user-team-channel-subscriptions db-conn username team-uuid))})))

(comment
  (get-user-team-subscirptions (database/get-pool) {:type :channel
                                :username "eva"
                                :team_uuid  #uuid "684062e0-4b68-4458-873e-6bc22ddbd925"})
  ,)

#_(teams/get-teams)
