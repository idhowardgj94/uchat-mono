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
   [taoensso.timbre :as timbre]))


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
            (users/insert-to-db db-conn username password name email)
            (timbre/info "insert user to default teams 'public'")
            (let [uuid (->> (teams/get-team-by-name db-conn "public")
                            first
                            :teams/uuid)]
              (teams/add-user-to-team db-conn username uuid))

            (json-response {:status "success"
                            :token (get-login-token username)}))))))

(defn login-handler
  [request]
  (let [body (:body request)
        valid? (s/valid? specs/post-login-spec body)
        db-conn (:db-conn request)]
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
            (let [claims {:username (keyword username)
                          :exp (.getMillis (time/plus (time/now) (time/seconds 3600)))}
                  token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
              (timbre/debug "success logined")
              (json-response {:token token}))))))))

