(ns com.howard.uchat.backend.auth.interface
  (:require
   [com.howard.uchat.backend.database.interface :as database]
   [buddy.core.nonce :as nonce]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]
   [next.jdbc :as jdbc]))

(defonce secret (atom nil))

(defn get-secret
  "get the secrupt store in auth"
  []
  @secret)
#_(reset! secret (nonce/random-bytes 32))
(defn setup-secret!
  "setup secret, get from database first, and if no secret found
  generate one and update to DB"
  []
  (let [result (next.jdbc/execute-one!
                (database/get-pool)
                ["SELECT secret FROM meta"])
        secret-data (:meta/secret result)]
    (if (nil? secret-data)
      (let [secret-data (nonce/random-bytes 32)]
        (reset! secret secret-data)
        (next.jdbc/execute-one!
         (database/get-pool)
         ["UPDATE meta SET secret = ?" secret-data]))
      (reset! secret secret-data))
    (get-secret)))

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
    (jwt/encrypt claims @secret {:alg :a256kw :enc :a128gcm})))

