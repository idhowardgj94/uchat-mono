(ns com.howard.uchat.backend.api-server.core
  (:require
   [com.howard.uchat.backend.database.interface :as database]
   [com.howard.uchat.backend.api-server.spec :as specs]
   [com.howard.uchat.backend.users.interface :as users]
   [com.howard.uchat.backend.subscriptions.interface :as subscriptions]
   [compojure.core :refer [GET POST context defroutes]]
   [next.jdbc.connection :as connection]
   [org.httpkit.server :as hk-server]
   [ring.middleware.defaults :as d]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.resource :refer [wrap-resource]]
   [clj-time.core :as time]
   [buddy.core.nonce :as nonce]
   [buddy.auth :refer [authenticated? throw-unauthorized]]
   [buddy.auth.backends.token :refer [jwe-backend]]
   [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
   [buddy.sign.jwt :as jwt]
   [ring.util.response :as response]
   [taoensso.timbre :as timbre]
   [clojure.spec.alpha :as s]
   [buddy.hashers :as hashers]))

;; (ns-unalias *ns* 'specs)
(defonce secret (nonce/random-bytes 32))

(def auth-backend (jwe-backend {:secret secret
                                :token-name "token"
                                :options {:alg :a256kw :enc :a128gcm}}))


(defn json-response
  "add response with header content-type: application/json"
  [body]
  (assoc-in (response/response body) [:headers "Content-Type"] "application/json"))

(defn home
  [request]
  (timbre/info "inside home")
  (if-not (authenticated? request)
    (throw-unauthorized)
      (json-response {:status "Logged" :message (str "hello logged user "
                                                     )})))

(defn get-login-token
  "Get login token for frontend.
  TODO: should I user keyword for username?
  "
  [username]
  (let [claims {:user (keyword username)
                :exp (.getMillis (time/plus (time/now) (time/seconds 3600)))}]
    (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})))

(defn wrap-cors
  [handler]
  (fn [request]
    (let [response (handler request)
          headers (conj (or (:headers response) {})
                              {"Access-Control-Allow-Origin" "*"
                               "Access-Control-Allow-Headers" "*"
                               "Access-Control-Allow-Methods" "*"})]
      
      (assoc response :headers headers))))

(defonce server (atom nil))

(defn index-handler
  [request]
  (timbre/info "inside index-handler" request)
  (json-response {:status "success"}))

(defn register-handler
  "handler register request"
  [request]
  (let [body (:body request)
        valid? (s/valid? specs/post-user-spec body)]
    (timbre/info body)
    (timbre/info valid?)
    (if-not valid?
      (do
        (timbre/info (str "inside not valid?: " valid?))
        (response/bad-request {:status "failed"
                               :error (str "Wrong body palyoad. please check the API docs. msg: "
                                            (s/explain-str specs/post-user-spec body)
                                            ", "
                                            (type body))}))
      (do (timbre/info "start to save user data to db")
          (let [{:keys [username name password email]} body]
            (users/insert-to-db username password name email)
            (json-response {:status "success"
                            :token (get-login-token username)}))))))

(defn login-handler
  [request]
  (let [body (:body request)
        valid? (s/valid? specs/post-login-spec body)]
    (if-not valid?
      (response/bad-request {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str specs/post-login-spec body))})
      (do
        (timbre/info (str "login user: " (:username body)))
        (let [{:keys [username password]} body
              result (users/get-user-by-username username)
              {real-password :password} result]
          (if (or (nil? real-password)
                  (= (-> (hashers/verify password real-password)
                         :valid) false))
            (response/bad-request {:message "Login failed, please check username and password."})
            (let [claims {:user (keyword username)
                          :exp (.getMillis (time/plus (time/now) (time/seconds 3600)))}
                  token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
              (json-response {:token token}))))))))

(defn get-subscriptions-handler
  [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    (let [params (:params request)
          valid? (s/valid? specs/get-subscriptions-spec params)]
      (if-not valid?
        (response/bad-request {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str specs/get-subscriptions-spec params))})
        (let [{:keys [type team-uuid]} params
              {:keys [:username]} (:identity request)]
          (subscriptions/get-user-team-subscriptions {:type type
                                                      :username username
                                                      :team-uuid team-uuid})
          
          )))))
(defroutes app-routes
  (context "/api/v1" []
           (POST "/register" [] register-handler)
           (POST "/login" [] login-handler)
           (GET "/subscriptions" [] get-subscriptions-handler))
  (GET "/home" [] home)
  (GET "/" [] index-handler))

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
  (reset! server
          (hk-server/run-server
           (-> #'app-routes
               (wrap-json-body  {:keywords? true :bigdecimals? true})
               (wrap-json-response  {:pretty false})
               (wrap-reload)
               (wrap-cors)
               (wrap-authorization auth-backend)
               (wrap-authentication auth-backend)
               (wrap-resource "public")
               (d/wrap-defaults d/api-defaults)
               )
           {:join? false :port 4000})))

(defn stop-server!
  "stop restful api server"
  []
  (@server))

(comment
  (start-server!)
  (stop-server!)
  ,)
