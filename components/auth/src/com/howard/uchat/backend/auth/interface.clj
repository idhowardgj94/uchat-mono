(ns com.howard.uchat.backend.auth.interface
  (:require
   [buddy.core.nonce :as nonce]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as time]))


(defonce secret (nonce/random-bytes 32))

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

