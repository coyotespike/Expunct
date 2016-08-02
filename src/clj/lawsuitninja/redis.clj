(ns lawsuitninja.redis
  (:require [taoensso.carmine :as car]
            [environ.core :refer [env]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params keyword-params-request]]
            [clojure.edn :as edn]
            [lawsuitninja.emails :refer [welcome-email
                                         password-email
                                         password-changed-email]])
  (:import org.apache.commons.codec.binary.Base64
           java.nio.charset.Charset))

;; (def conn-spec {:host "127.0.0.1"
;;                 :port 6379
;;                 :timeout 8000})
;; (if-let [uri (env :redistogo-url)]
;;                          {:uri uri}
;;                          (env :redis))})

(def redis-address
"redis://rediscloud@pub-redis-13388.us-east-1-4.5.ec2.garantiadata.com:13388")
(def redis-conn {:pool {}
                 :host redis-address})

;; Redis should look like:
;; username {:password password :innerMap BigAtom}}

(defmacro wcar* 
  "A macro to include the redis-conn with any Carmine function"
  [& body] `(car/wcar redis-conn ~@body))

;;; Called from: login-user or register-user
(defn read-from-Redis 
  "This should be called only for a registered user who is not logged in."
  [username]
  (wcar* (car/get username)))

(defn change-password
  "This changes the password to the given value."
  [request]
  (let [password (get-in request [:body :password])
        username (get-in request [:body :username])
        old-db (read-from-Redis username)
        new-db (assoc old-db :password password)]
    (do
      (password-changed-email username)
      (wcar* (car/set username new-db)))))

;;; Called directly
(defn update-Redis 
  "This should be called only by a logged-in user who is hitting the save button."
  [request]
  (let [username (get-in request [:body :username])
        password (:password (read-from-Redis username))
        BigAtom (get-in request [:body :BigAtom])]
    (wcar* (car/set username {:password password :BigAtom BigAtom}))))


;;; Called from: login-or-register
(defn write-to-Redis
  "This should be called only by a new user."
  [username password BigAtom]
  (wcar* (car/set username {:password password :BigAtom BigAtom})))

(defn login-user 
  "If this function doesn't find the user, it asks them to register.
  If it does, then it checks the password. In case of error, it notifies
  the user. If the password matches, it returns the user's data."
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        BigAtom (get-in request [:body :BigAtom])
        user-data (read-from-Redis username)]
    (cond
      (nil? user-data) {:status 204 :body (pr-str 
"We didn't find you in our database. Do you need to register?")}
      (not= password (:password user-data))
            {:status 403 :body (pr-str 
"That email is already in our database, but the password didn't match.")}
       (= password (:password user-data))
          {:status 200 :body (pr-str (:BigAtom user-data))}

          :else {:status 600 :body "oh gosh"})))

(def alphanumeric "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")
(defn get-random-id [length]
  (loop [acc []]
    (if (= (count acc) length) (apply str acc)
      (recur (conj acc (rand-nth alphanumeric))))))


(defn random-password [request]
  (let [username (get-in request [:body :username])
        user-data (read-from-Redis username)
        password (get-random-id 12)
        new-db (assoc user-data :password password)]
    (if (some? user-data)
      (do
        (wcar* (car/set username new-db))
        (password-email username password))
      {:status 404 :body "Actually, we didn't find that email. Are you sure you've registered?"})))

;;; Called directly
(defn register-user
  "If the username doesn't exist, this function will create an entry in
  the database. If it does, it directs the user to the login page."
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        firstname (get-in request [:body :firstname])
        lastname (get-in request [:body :lastname])
        BigAtom (get-in request [:body :BigAtom])
        user-data (read-from-Redis username)]

    (cond 
      (nil? user-data) 
      (do
        (welcome-email username firstname lastname)
        (write-to-Redis username password BigAtom)
        {:status 200 :body (pr-str BigAtom)})
      (some? user-data)
      {:status 303 :body (pr-str "Looks like we've already got you. Need to login?")}
       :else {:status 600 :body "oh gosh"})))

(defn delete-user
  "This deletes the user and data from the database. Use with care!"
  [request]
  (let [username (get-in request [:body :username])
        password (get-in request [:body :password])
        user-data (read-from-Redis username)]
    (if (= password (:password user-data))
      (do
        (wcar* (car/del username))
        {:status 200 :body (pr-str "Your account has been deleted.")})
      {:status 204 :body (pr-str "Sorry, that password didn't match.")})))


(def clojure-db (atom {}))
(defn put-db 
  "These are for simple testing of round-trips features"
  [req]
  (if (map? req)
      (swap! clojure-db #(merge-with merge % {:request (pr-str req)}))
      (swap! clojure-db #(merge-with merge % {:false (pr-str req)}))))
(defn get-db "The matching function for simple testing."  [] @clojure-db)
