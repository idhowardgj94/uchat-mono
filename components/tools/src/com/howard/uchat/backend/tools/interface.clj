(ns com.howard.uchat.backend.tools.interface
  "Thise component collect some useful tools in this code base,
  Include connection test tool, etc"
  (:require
   [clj-http.client :as client]
   [jsonista.core :as json]
   [com.howard.uchat.backend.api-server.auth :as auth]))

(defprotocol GenerateToken
  (get-token [this] "get token")
  (generate-token [this] "generate auth token."))

(defprotocol Post
  (post [this endpoint body] "body is a map that contain body parameters."))

(defprotocol Get
  (get_ [this endpoint query-params] [this endpoint]
    "query param is a map that contain query parameters, currently use get_ to avoid conflict with clojure.core"))

(defn test--generate-auth-header
  "this is a help function to generate a valid header."
  [username name]
  {:headers {"authorization" (str "token " (auth/generate-auth-token username name))}})

(defrecord Client [username password]
  GenerateToken
  (generate-token [this]  (merge this {:token (test--generate-auth-header username password)}))
  (get-token [this] (:token this))
  Post
  (post [this endpoint body]
    (let [host (:host this)]
      (client/post (str host endpoint)
                   (merge (:token this)
                          {:content-type :json
                           :body (json/write-value-as-string body)}))))
  Get
  (get_ [this endpoint query-params]
    (let [host (:host this)]
      (client/get (str host endpoint)
                  (merge (:token this)
                         {:query-params query-params}))))
  (get_ [this endpoint]
    (get_ this endpoint nil)))

(defn new-client
  "new client, just for test."
  ^Client [^String username ^String  name]
  (-> (Client. username name)
      (generate-token)
      (assoc :host "http://localhost:4000")))

#_(let [c (new-client "idhowardgj94" "howard")]
  (post c "/api/v1/channels" {:channel-name "bar"
                              :team-id "123"}))
