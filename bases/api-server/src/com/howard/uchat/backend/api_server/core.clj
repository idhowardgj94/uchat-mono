(ns com.howard.uchat.backend.api-server.core
  (:require
   [com.howard.uchat.backend.database.interface :as database]
   [com.howard.uchat.backend.api-server.spec :as specs]
   [com.howard.uchat.backend.users.interface :as users]
   [compojure.core :refer :all]
   [next.jdbc.connection :as connection]
   [org.httpkit.server :as hk-server]
   [ring.middleware.defaults :refer :all]
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
(def authdata {:admin "secret"
               :test "secret"})

(def auth-backend (jwe-backend {:secret secret
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
    (do
      (json-response {:status "Logged" :message (str "hello logged user "
                                                     )}))))
(defn login
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        valid? (some-> authdata
                       (get (keyword username))
                       (= password))]
    (timbre/info request)
    (timbre/info username ", " password)
    (timbre/info (str (time/plus (time/now) (time/seconds 3600))))
    (if valid?
      (let [claims {:user (keyword username)
                    :exp (.getMillis (time/plus (time/now) (time/seconds 3600)))}
            token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
        (timbre/info "token here:" token)
        (response/response {:token token}))
      (response/bad-request {:message "wrong auth data"}))))

(defn wrap-cors
  [handler]
  (fn [request]
    (assoc-in (handler request) [:headers "Access-Control-Allow-Origin"] "*")))


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
    (if-not valid?
      (response/bad-request {:status "Wrong body palyoad. please check the API docs"})
      (do (timbre/info "start to save user data to db")
          (let [{:keys [username name password email]} body]
            (users/insert-to-db username password name email)
            (json-response {:status "success"}))))))

(s/explain-str specs/post-login-spec {:usernam "12" :password "222"})
(defn login-handler
  [request]
  (let [body (:body request)
        valid? (s/valid? specs/post-login-spec body)]
    (timbre/info (s/explain-str specs/post-login-spec body))
    (if-not valid?
      (response/bad-request {:message  (str "Wrong body palyoad. please check the API docs. msg:" (s/explain-str specs/post-login-spec body))})
      (do
        (timbre/info (str "login user: " (:username body)))
        (let [{:keys [username password]} body
              result (users/get-user-by-username username)
              {real-password :password} result
              login? (hashers/verify password real-password)]
          (if (nil? login?)
            (response/bad-request {:message "Login failed, please check username and password."})
            (let [claims {:user (keyword username)
                          :exp (.getMillis (time/plus (time/now) (time/seconds 3600)))}
                  token (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm})]
              (json-response {:token token}))))))))

(defroutes app-routes
  (context "/api/v1" []
           (POST "/register" [] register-handler)
           (POST "/login" [] login-handler))
  (POST "/login" [] login)
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
               (wrap-defaults api-defaults)
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

#_(
   (json/write-str {:a "hello"} )
   (require '[clj-http.client :as client])
   (client/get "http://localhost:4000/")
   (client/post "http://localhost:4000/login" {:content-type :json
                                               :body
                                               (json/write-str {:username "admin"
                                                                :password "secret"})})
   (client/post
    "http://localhost:4000/api/v1/register"
    {:content-type :json
     :body
     (json/write-str {:username "test2"
                      :password "test2"
                      :name "test2"
                      :email "test2@gmail.com"})})
   (client/post
    "http://localhost:4000/api/v1/login" {:content-type :json
                                          :body
                                          (json/write-str {:username "test2"
                                                           :password "test2"})})
   (client/get
    "http://localhost:4000/home"
    {:headers
     {"authorization" "Token eyJhbGciOiJBMjU2S1ciLCJlbmMiOiJBMTI4R0NNIn0.Vdoc_BWUEk8s5V_h_iQN08RkHOSC4YIk.bmZ_JiP465g5Si-P.d1ClznhuZ4Hk4JaWqiacJvlZfzgZyQlI6otMZ7I0J8AAvL34bnjx4b_QTRSvQ_SftBua0A.D3B4bNWxXHNhT8eXHBK_2g"}})
   ,)
