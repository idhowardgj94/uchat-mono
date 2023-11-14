(ns com.howard.uchat.backend.api-server.auth
  (:require
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [buddy.core.nonce :as nonce]
   [clj-time.core :as time]
   [clojure.spec.alpha :as s]
   [com.howard.uchat.backend.api-server.spec :as specs]
   [com.howard.uchat.backend.teams.interface :as teams]
   [com.howard.uchat.backend.users.interface :as users]
   [ring.util.response :as response]
   [com.howard.uchat.backend.api-server.util :refer [json-response]]
   [taoensso.timbre :as timbre]
   [next.jdbc :as jdbc]
   [ring.util.response :as res-util]
   [com.howard.uchat.backend.database.interface :as database]
   [jsonista.core :as json]))


(defonce secret (nonce/random-bytes 32))
(defn get-login-token
  "Get login token for frontend.
  TODO: should I user keyword for username?
  "
  [username]
  (let [claims {:username (keyword username)
                :exp (.getMillis (time/plus (time/now) (time/seconds 3600)))}]
    (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})))

(defn register-handler
  "handler register request
  TODO: which team should the user register? for now we add a team public and make
  every user into this team."
  [request]
  (let [body (:body request)
        db-conn (:db-conn request)
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
            (jdbc/with-transaction [tx db-conn]
              (if (some? (users/get-user-by-username tx username))
                (json-response {:status "failed"
                                :message "username have already been registered."})
                (do (users/insert-to-db tx username password name email)
                    (timbre/info "insert user to default teams 'public'")
                    (let [uuid (->> (teams/get-team-by-name tx "public")
                                    first
                                    :teams/uuid)]
                      (teams/add-user-to-team tx username uuid))
                    (json-response {:status "success"
                                    :token (get-login-token username)})))))))))

(defrecord Claims [username name exp])

(defn- new-exp
  "generate new expire time, given:
  seconds
  params:
  seconds - int"
  ^org.joda.time.PeriodType [seconds]
  (.getMillis (time/plus (time/now) (time/seconds seconds))))

(defn generate-auth-token
  "generate auth token for login.
  params:
  username - string
  name - string
  exp default 3600 timestamp in millis"
  [username name]
  (let [claims (Claims. username name (new-exp 3600))]
    (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})))


(defn login-handler
  [request]
  (let [body (:body request)
        valid? (s/valid? specs/post-login-spec body)
        db-conn (:db-conn request)]
    (timbre/info "inside login-handler")
    (if-not valid?
      (response/bad-request {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str specs/post-login-spec body))})
      (do
        (timbre/info (str "login user: " (:username body)))
        (let [{:keys [username password]} body
              result (users/get-user-by-username db-conn username)
              {real-password :password} result]
          (if (or (nil? real-password)
                  (= (-> (hashers/verify password real-password)
                         :valid) false))
            (response/bad-request {:message "Login failed, please check username and password."})
            (do
              (timbre/debug "success logined")
              (json-response {:token (generate-auth-token (keyword username) (:name result))}))))))))

