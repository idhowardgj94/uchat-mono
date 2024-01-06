(ns com.howard.uchat.backend.tools.interface
  "Thise component collect some useful tools in this code base,
  Include connection test tool, etc"
  (:require
   [clj-http.client :as client]
   [jsonista.core :as json]
   [com.howard.uchat.backend.tools.macro :as macro]
   [camel-snake-kebab.core :as cbk]
   [potemkin :refer [import-vars]]
   [com.howard.uchat.backend.auth.interface :as auth]))

(import-vars [macro export-fn])

;; TODO: client should be put in auth 
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
  "new client, just for test.
  params
  opt - port: number host: string "
  (^Client [^String username ^String name]
   (new-client username name {}))
  (^Client [username name {:keys [host port]}]
   (let [h (if (some? host) host "http://localhost")
         p (if (some? port) port 4000)]
     (-> (Client. username name)
         (generate-token)
         (assoc :host (str h ":" p))))))

;; TODO map
(declare reduce-map-to-kebab-case!)
(defn reduce-vector-to-kebab-case!
  "a small helper function to reduce vector to kebab case
  new-v must be a ttrasient collection."
  [new-v v]
  (reduce
   (fn [res v]
     (conj! res (if (map? v)
                  (-> (reduce-map-to-kebab-case! (transient {}) v)
                      persistent!)
                  v)))
   new-v
   v))

(defn reduce-map-to-kebab-case!
  "a small warper for reduce map key to kebab case.
   new body neet to be transient collection"
  [new-body body]
  (reduce
   (fn [res [k v]]
     (assoc! res
             (cbk/->kebab-case k)
             (if (vector? v)
               (persistent! (reduce-vector-to-kebab-case! (transient []) v))
               v)))
   new-body
   body))
#_(persistent! (reduce-map-to-kebab-case! (transient {}) {:fooBar "foo"
                                                          :hello [{:helloWord "hi"}
                                                                  {:oneMore [{:testAgain "123"}]}]}))
#_(let [c (new-client "idhowardgj94" "howard")]
  (post c "/api/v1/channels" {:channel-name "bar"
                              :team-id "123"}))
