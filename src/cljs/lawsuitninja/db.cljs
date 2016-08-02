(ns lawsuitninja.db
  (:require 
   [dommy.core :refer-macros [sel sel1]]
   [dommy.core :as dommy]
   [cljs.core.async :refer [<!]]
   [enfocus.core :as ef]
   [cljs-http.client :as http]
   [schema.core :as s 
    :include-macros true]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [cljs.reader :as reader]
   [cljs.core.async :as async :refer [put! <! >! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn get-position []
  (let [out (chan)
        geo (.-geolocation js/navigator)]
    (.getCurrentPosition geo (fn [pos] (put! out pos)))
    out))

(defn print-position []
  (go 
    (let [coords    (.-coords (<! (get-position)))
          latitude  (.-latitude coords)
          longitude (.-longitude coords)]
      (.log js/console "Lat:" latitude "Long:" longitude))))



;;;; Functions to find the anti-forgery-token
(defn anti-forgery-token []
  (-> :#anti-forgery-token
          sel1))
(defn token []
  (ef/from (anti-forgery-token) (ef/get-attr :value)))
;;;; ----------- End anti-forgery-token

(defn email-us [email-key]
  (let [user-message (subscribe [:contact])
        user-email (subscribe [:your-email])]
  (http/post "/email" {:transit-params {:user-message @user-message 
                                        :email @user-email 
                                        :email-key email-key}
                       :headers {"x-csrf-token" (token)}})))


(defn pay-up []
  (let [user-message (subscribe [:contact])
        user-address (subscribe [:your-address])
        user-name (subscribe [:yourname])
        user-email (subscribe [:your-email])]
  (http/post "/payme" {:transit-params {:user-message @user-message 
                                        :user-address @user-address
                                        :name @user-name
                                        :user-email @user-email}
                         :headers {"x-csrf-token" (token)}})))
 

(defn get-by-username [password]
  (go
  (let [username (subscribe [:your-email])
        response (<! (http/get "/redis-get" {:query-params {:key1 @username}}))
        new-db (:body response)]
    (dispatch [:process-service-1-response new-db]))))

(defn success?
  "treat any 2xx status code as successful"
  [status]
  (if (and (>= status 200) (< status 300))
    true
    false))


(defn update-for-success 
  [new-db]
    (if (map? new-db)
      (do
        (js/console.log (pr-str new-db))
        (dispatch [:process-service-1-response new-db])
        (dispatch [:change-logged-in? true])
        (set! (.-hash js/window.location) "/dashboard"))
      (do
        (js/console.log (pr-str new-db))
        (js/alert "Oh Gosh. We messed up. Click 'Contact Us' to let us know!"))))


(defn update-for-failure [new-db]
  (if (string? new-db)
    (js/alert new-db)
    (js/alert "Sorry, something went wrong! Please contact us to tell us what.")))

(defn saved-alert [message]
  (js/alert message))


(defn register-user 
  "This function is called from a page which requires the first and last name
  and therefore enables the app to display the names after they log in."
  [password]
  (go
    (let [BigAtom (subscribe [:app-db])
          username (subscribe [:your-email])
          first-name (subscribe [:first-name])
          last-name (subscribe [:last-name])
          plain-value {:username @username :firstname @first-name :lastname @last-name :password password :BigAtom @BigAtom}
          response (<! (http/post "/registerme" {:transit-params plain-value
                                                :headers {"x-csrf-token" (token)}}))
          new-db (reader/read-string (:body response))
          status (:status response)]
      (cond
        (success? status) (update-for-success new-db)
        (= 303 status) (js/alert new-db)
        :else (update-for-failure new-db)))))

(defn lookup-record
  "This function just sends us the user's information to look up."
  [emailkey]
  (go
    (let [BigAtom (subscribe [:app-db])
          plain-value {:email-key emailkey :BigAtom @BigAtom}
          response (<! (http/post "/email" {:transit-params plain-value
                                            :headers {"x-csrf-token" (token)}}))
          new-db (reader/read-string (:body response))
          status (:status response)]
      status)))

(defn login-user 
  "This function works the same as register-user, above. But the response will
  return 204 if the user doesn't have an account, and so they must be told or
  redirected to the registration page."
  [password forgot-password]
  (go
    (let [BigAtom (subscribe [:app-db])
          username (subscribe [:your-email])
          plain-value {:username @username :password password :BigAtom @BigAtom}
          response (<! (http/post "/logmein" {:transit-params plain-value
                                                :headers {"x-csrf-token" (token)}}))
          new-db (reader/read-string (:body response))
          status (:status response)
          forgot-pass? (fn [x] (= x 403))]
      (cond
        (= 200 status) (update-for-success new-db)
        (= 204 status) (js/alert "We didn't find you in our database. Do you need to register?")
        (forgot-pass? status) (swap! forgot-password not)
        :else (update-for-failure new-db)))))


(defn email-random-password 
  []
  (go 
    (let [username (subscribe [:your-email])
          value-sent {:username @username}
          response (<! (http/post "/emailrandompassword" 
                                  {:transit-params value-sent
                                   :headers {"x-csrf-token" (token)}}))
          status (:status response)
          message (:body response)]
      (if (success? status)
        (saved-alert "We sent you a new password.")
        (do 
          (js/console.log @username)
          (update-for-failure "Gadzooks! We've failed! Contact us about the problem!"))))))


(defn new-password 
  "Allows a logged-in user, from their profile, to overwrite the password 
  value in Redis, on the server. The password atom is already dereferenced."
  [password]
  (go 
    (let [username (subscribe [:your-email])
          value-sent {:username @username :password password}
          response (<! (http/post "/updatepassword" 
                                  {:transit-params value-sent
                                   :headers {"x-csrf-token" (token)}}))
          status (:status response)
          message (:body response)]
      (if (success? status)
        (saved-alert "Your password has been updated.")
        (update-for-failure "Gadzooks! We've failed! Contact us about the problem!")))))

(defn delete-profile 
  "Allows a logged-in user, from their profile, to overwrite the password 
  value in Redis, on the server. The password atom is already dereferenced."
  [password]
  (go 
    (let [username (subscribe [:your-email])
          value-sent {:username @username :password password}
          response (<! (http/post "/deleteprofile" 
                                  {:transit-params value-sent
                                   :headers {"x-csrf-token" (token)}}))
          status (:status response)
          message (:body response)]
      (if (success? status)
        (do
          (saved-alert "Your account has been deleted.")
          (dispatch [:initialise-db]))
        (update-for-failure "Gadzooks! We've failed! Contact us about the problem!")))))


(defn update-us 
  "Allows a logged-in user to hit 'Save', and overwrite the BigAtom value in
  Redis, on the server."
  []
  (go 
    (let [BigAtom (subscribe [:app-db])
          username (subscribe [:your-email])
          plain-value {:username @username :BigAtom @BigAtom}
          response (<! (http/post "/savemywork" 
                                  {:transit-params plain-value
                                   :headers {"x-csrf-token" (token)}}))
          status (:status response)]
      (if (success? status)
;        (do
          (js/console.log (pr-str response))
;          (saved-alert "Saved!")
        (update-for-failure "Gadzooks! We've failed! Contact us about the problem!")))))


;; Correct shape of db - simply a map, with strings or ints as values.
(def schema           
  {s/Keyword s/Any})

(defn valid-schema?
  "validate the given db, writing any problems to console.error"
  [db]
  (let [res (s/check schema db)]
    (if (some? res)
      (.error js/console (str "schema problem: " res)))))


(def default-value
  {   
   :typeahead {:hidden? true
               :mouse-on-list? false
               :index 0
               :selections []
               :value nil}
   :expunction-purchase "Not yet!"
   :background-check-purchase "Not yet!"

   :case-disposition ""
   :dismissal-date ""

   :related-charges ""
   :arrest-county "County of arrest"
   :arrest-date "Date of arrest"
   :birthdate "Date of birth"
   
   :first-name "Your first name"
   :last-name "Your last name"

   :yourname "Full legal name"
   :your-signature "Your signature"
   :your-printed-name "Your printed name"
   :your-address "Your address"
   :city-state "Your City, State, Zip"
   :your-phone "Your phone number"
   :your-email "Your email here"
   :contact "Your question here"

   :state "State you live in"
    :logged-in? false
   })
