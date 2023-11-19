
(ns com.howard.uchat.backend.api-server.core
  (:require
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.auth.middleware :refer [wrap-authorization]]
   [clj-http.client :as client]
   [clojure.spec.alpha :as s]
   [com.howard.uchat.backend.api-server.auth
    :refer [login-handler register-handler secret]]
   [com.howard.uchat.backend.api-server.endpoints.channels :as channels-endpoint]
   [com.howard.uchat.backend.api-server.endpoints.messages :as messages-endpoint]
   [com.howard.uchat.backend.api-server.endpoints.teams :as teams-endpoint]
   [com.howard.uchat.backend.api-server.middleware
    :refer [wrap-authentication-guard wrap-cors wrap-database
            wrap-token-from-params]]
   [com.howard.uchat.backend.api-server.spec :as specs]
   [com.howard.uchat.backend.api-server.util :refer [json-response] :as util]
   [com.howard.uchat.backend.channels.interface :as channels]
   [com.howard.uchat.backend.database.interface :as database]
   [com.howard.uchat.backend.socket.interface :as socket]
   [com.howard.uchat.backend.subscriptions.interface :as subscriptions]
   [com.howard.uchat.backend.teams.interface :as teams]
   [com.howard.uchat.backend.users.interface :as users]
   [compojure.core :refer [context defroutes GET POST routes wrap-routes]]
   [jsonista.core :as json]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   [org.httpkit.server :as hk-server]
   [ring.middleware.defaults :as d]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.util.response :as response]
   [taoensso.timbre :as timbre]))

(def auth-backend 
  (jwe-backend {:secret secret
                :token-name "token"
                :options {:alg :a256kw :enc :a128gcm}}))
#_[ns-unalias *ns* 'socket]

(defn home
  [request]
  (timbre/info "inside home")
  (if-not (authenticated? request)
    (throw-unauthorized)
    (json-response {:status "Logged" :message (str "hello logged user ")})))

(defonce server (atom nil))

(defn index-handler
  [request]
  (timbre/info "inside index-handler" request)
  (json-response {:status "success"}))

(defn get-subscriptions-handler
  [request]
  (let [params (:params request)
        db-conn (:db-conn request)
        valid? (s/valid? specs/get-subscriptions-spec params)]
    (if-not valid?
      (response/bad-request {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str specs/get-subscriptions-spec params))})
      (let [{:keys [type team_uuid]} params
            {:keys [username]} (:identity request)]
        (json-response (subscriptions/get-user-team-subscriptions db-conn {:type (keyword type)
                                                                   :username username
                                                                   :team-uuid  (parse-uuid team_uuid)}))))))

(defn post-gen-direct-handler
  [request]
  (let [username (-> (:identity request) :username)  
        body (:body request)
        valid? (s/valid? specs/post-generate-direct-spec body)
        conn (:db-conn request)
        other-user (:other_user body)]
    (if-not valid?
      (response/bad-request {:message (str "Wrong body palyoad. please check the API docs. msg: " (s/explain-str specs/post-generate-direct-spec body))})
      (if (nil? (users/get-user-by-username conn other-user))
        (response/bad-request {:message (str "Can't find the user " other-user)})
        (jdbc/with-transaction [tx (database/get-pool)]
          (let [channel-uuid (channels/create-direct-and-insert-users tx (-> (:team_uuid body) parse-uuid) username (:other_user body))]
            (subscriptions/create-direct-subscriptions tx channel-uuid username other-user)
            (util/json-response {:status "success"
                                 :result channel-uuid})))))))

(defn get-me-handler
  [request]
  (let [username (-> request :identity :username)
        conn (-> request :db-conn)]
    (util/json-response (dissoc (users/get-user-by-username conn username)
                                :password))))

(defroutes app-routes
  (context "/api/v1" []
    (wrap-routes
     (routes
      (GET "/subscriptions" [] get-subscriptions-handler)
      (POST "/direct/generate" [] post-gen-direct-handler)
      (GET "/me" [] get-me-handler))
     wrap-authentication-guard)
    (GET "/chsk" [] socket/ring-ajax-get-or-ws-handshake)
    (POST "/chsk" [] socket/ring-ajax-post)
    (POST "/register" [] register-handler)
    (POST "/login" [] login-handler))
  (GET "/home" [] home)
  (GET "/" [] index-handler)
  #'teams-endpoint/teams-routes
  #'messages-endpoint/messages-routes
  #'channels-endpoint/channel-routes)

(defn setup-default-teams
  "setup a default team, for now everyone will register into this team.
  default: public
  TODO"
  []
  (let [db-conn (database/get-pool)]
    (timbre/info "Check if default team 'public' exist or not......")
    (when-not
     (teams/is-team-exist? db-conn "public")
      (timbre/info "'public' is not exist in uchat, create it.")
      (teams/create-team-by-name db-conn "public"))))

(defn start-server!
  "start a restful api server,
  store it to server"
  []
  (database/init-database
   {:jdbcUrl
    (connection/jdbc-url {:host "localhost"
                          :dbtype "postgres"
                          :dbname "uchat"
                          :useSSL false})
    :username "postgres" :password "postgres"})
  (setup-default-teams)
  (socket/start-router!)
  (reset! server
          (hk-server/run-server
           (-> #'app-routes
               (wrap-json-body  {:keywords? true :bigdecimals? true})
               (wrap-json-response  {:pretty false})
               (wrap-reload)
               (wrap-database)
               (wrap-cors)
               (wrap-authorization auth-backend)
               #_(wrap-authentication auth-backend)
               (wrap-token-from-params auth-backend)
               (wrap-resource "public")
               (d/wrap-defaults d/api-defaults))
           {:join? false :port 4000})))

(defn stop-server!
  "stop restful api server"
  []
  (@server))

(defn restart-server!
  []
  (stop-server!)
  (start-server!))

(comment
  (require '[clj-http.client :as client])
  (require '[jsonista.core :as json])
  (def auth {:headers {"authorization" "token eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMTI4R0NNIn0.12Y59mvi-pdLuo9GO8q8AFIXtX03ywlC.hNkmh-0EeejEmjDH.5IIEcdh-4u5G--tzQ1BWUD7z4_S7p-exs4wXkW-im32YHJOwam0YSjOCFxJg2Lo.PjIMZakgbJGXSlkuC-UvAQ"}})

  (teams/get-teams (database/get-pool))
  (client/post "http://localhost:4000/api/v1/login"
               {:content-type :json
                :body
                (json/write-value-as-string
                 {:username "idhowardgj94"
                  :password "123456"})})
  (client/get "http://localhost:4000/api/v1/channels/6a77e68b-4d47-4520-b0b4-a8421edc1041/messages"
              (merge auth
                     {:content-type :json}))
  (client/post "http://localhost:4000/api/v1/direct/generate"
               (merge auth
                      {:content-type :json
                       :body (json/write-value-as-string
                              {:team_uuid "d205e510-8dee-4fb5-8d55-4f4bc5174bad"
                               :other_user "eva"})}))

  (client/get (str "http://localhost:4000/api/v1/messages/6e05177f-5e71-49ed-8004-2e70ed0dda40") (merge auth {:content-type :json}))

  (teams/get-team-by-name (database/get-pool) "public")
  (client/post "http://localhost:4000/api/v1/register"
               {:content-type :json
                :body
                (json/write-value-as-string
                 {:username "idhowardgj94"
                  :password "123456"
                  :name "idhowardgj94"
                  :email "idhowardgj94@gmail.com"})})
  (start-server!)
  (stop-server!))
