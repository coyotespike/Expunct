(ns lawsuitninja.slacker
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

(defn join-slack [request]
  (let [email (get-in request [:body :username])]
    (http/post "https://lawsuitninja-community.slack.com/api/users.admin.invite" 
               {:form-params
                {:email email
                 :token "xoxp-8524545570-8524577620-8524809990-e3e0e7"
                 :set_active "true"}})))

