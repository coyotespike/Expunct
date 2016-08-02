(ns lawsuitninja.emails
  (:require
   [postal.core :refer [send-message]]
   [clj-time.core :as t]
   [clj-time.local :as l]
   [clj-time.format :as f]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [net.cgrand.enlive-html :refer [deftemplate 
                                   set-attr 
                                   html-resource
                                   defsnippet
                                   substitute
                                   at
                                   emit*]])

  (import java.io.StringReader))


(def host-email "User email for gmail hosting" 
  "example@expunct.com")
(def pass "my gmail password"
  "example")
(def our-emails "The user's messages will be sent to these addresses."
  ["example@expunct.com"])

(def conn "The configuration for Gmail's hosting."
  {:host "smtp.gmail.com"
   :ssl true
   :user host-email
   :pass pass})

(def full-day "Uses the clj-time library to get the day of the week."
  (f/formatter "EEEE"))
(def my-time-now "Gets a normal-looking local time"
  (l/format-local-time (l/local-now) :hour-minute))
(def my-today "Actually turns the 'EEEE' into a real word."
  (f/unparse full-day (l/local-now)))
(def tomorrow "Does the same for tomorrow."
  (f/unparse full-day (t/plus (l/local-now) (t/hours 24))))

(def plain-message "Sent if the user email can't support HTML."
  (str "We got your message at " my-time-now " on " my-today ", so we'll get back to you by " my-time-now ", " tomorrow ". If we haven't replied to your message by then, use the Pay Up button to get your $5."))
(def state-poll-message 
  "Thanks a lot for letting us know where you are. We'll get to your state as soon as we can. If you have any questions, you can talk to Tim or Rhiannon at (512) 921-3505, or just reply to this email. Bye for now!")
(def state-poll-tiny-text
  "Good to hear from you!")
(def state-poll-tiny-text2
  "Hope to talk to you soon!")
(def welcome-message "Sent if the user email can't support HTML."
    (str "Welcome to Expunct.com! We're excited to have you with us, and we look forward to helping you clear your record. We'll send you another email soon to let you know what we found in the Texas database. Please feel free to ask any question, and we'll get back to you within 24 hours."))

(defmulti email-body identity)
(defmethod email-body :contact-us [_]
  (let [replacement-text [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [plain-message]}]
        e-parsed (html-resource "templates/email.html")
        replaced (at e-parsed [:#whatWhat] (substitute replacement-text))
        finished (apply str (emit* replaced))]
    finished))
(defmethod email-body :state-poll [_]
  (let [replacement-text [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [state-poll-message]}]
        e-parsed (html-resource "templates/email.html")
        replaced (at e-parsed 
                     [:#whatWhat] (substitute replacement-text)
                     [:#tinyText] (substitute state-poll-tiny-text)
                     [:#tinyText2] (substitute state-poll-tiny-text2)
                     [:#firstButton] nil
                     [:#secondButton] nil)
        finished (apply str (emit* replaced))]
    finished))
(defmethod email-body :welcome [_]
  (let [replacement-text [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [welcome-message]}]
        e-parsed (html-resource "templates/email.html")
        replaced (at e-parsed 
                     [:#whatWhat] (substitute replacement-text)
                     ;; [:#tinyText] (substitute state-poll-tiny-text)
                     ;; [:#tinyText2] (substitute state-poll-tiny-text2)
                     [:#firstButton] nil
                     [:#secondButton] nil)
        finished (apply str (emit* replaced))]
    finished))


(defmulti send-email (fn [body] (:email-key body)))
(defmethod send-email :contact-us [body]
  (let [user-email (:email body)
        user-message (:user-message body)]
    ;; Email to user
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject "Thanks for your message!"
                   :body [:alternative
                          {:type "text/plain"
                           :content plain-message}
                          {:type "text/html"
                           :content (email-body :contact-us)}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Your users love you!"
                        :body (str user-email " says: " user-message)})))

(defmethod send-email :state-poll [body]
  (let [user-email (:email body)
        user-message (:user-message body)]
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject "Thanks for your message!"
                   :body [:alternative
                          {:type "text/plain"
                           :content state-poll-message}
                          {:type "text/html"
                           :content (email-body :state-poll)}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Your users love you!"
                        :body (str user-email " says: " user-message)})))

(defmethod send-email :welcome [body]
  (let [big-atom (:BigAtom body)
        user-email (:your-email big-atom)
        firstname (first (s/split (:yourname big-atom) #" "))
        got-name? (not= (:yourname big-atom) "Full legal name")
        nice-subject (str firstname ", a journey of a thousand miles begins with a single step")
        plain-subject "A journey of a thousand miles begins with a single step"]
    ;; Email to user
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject (if got-name? nice-subject plain-subject)
                   :body [:alternative
                          {:type "text/plain"
                           :content welcome-message}
                          {:type "text/html"
                           :content (email-body :welcome)}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Look'em if they booked'em"
                        :body (str "Storytime. " 
                                   (:yourname big-atom) " was born on "
                                   (:birthdate big-atom) " and was arrested on "
                                   (:arrest-date big-atom) " in "
                                   (:arrest-county big-atom) " , Texas. Their phone number is "
                                   (:your-phone big-atom) " and their email address is "
                                   (:your-email big-atom) ". Hope we can help!")})))


(def pay-message "Sent if the user email can't support HTML."
  (str "Yikes! You'll bankrupt us! Let us check out your request for information, and if we didn't respond, we'll mail you a little Abe Lincoln to be your friend. Talk to you soon!"))

(def pay-replacement-text "HTML with the right times and days of the week."
  [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [  (str "Yikes! You'll bankrupt us! Let us check out your request for information, and if we didn't respond, we'll mail you a little Abe Lincoln to be your friend. Talk to you soon!")]}])
(def pay-parsed "Turns the HTML email template into Enlive code."
  (html-resource "templates/pay-email.html"))

(def pay-replaced "Replaces the default HTML with the better HTML."
  (at pay-parsed [:#whatWhat] (substitute pay-replacement-text)))
(def pay-finished "Turns the finished Enlive code back into real HTML."
  (apply str (emit* pay-replaced)))


(defn pay-email
  "Lets the user know we have their request for money, and will pay soon."
  [body]
  (let [user-address (:user-address body)
        user-message (:user-message body)
        user-name (:name body)
        user-email (:user-email body)]
    ;; Email to user
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject "Money money money!"
                   :body [:alternative
                          {:type "text/plain"
                           :content pay-message}
                          {:type "text/html"
                           :content pay-finished}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Someone wants money!!"
                        :body (str user-name " says: " user-message
                                   " and they live at: " user-address)})))



(def welcome-replacement-text "HTML with the right times and days of the week."
  [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [welcome-message]}])
(def welcome-parsed "Turns the HTML email template into Enlive code."
  (html-resource "templates/welcome-email.html"))

(def welcome-replaced "Replaces the default HTML with the better HTML."
  (at welcome-parsed [:#whatWhat] (substitute welcome-replacement-text)))
(def welcome-finished "Turns the finished Enlive code back into real HTML."
  (apply str (emit* welcome-replaced)))


(defn welcome-email
  "Welcomes the user to Lawsuit Ninja."
  [user-email firstname lastname]
  (let [nice-subject (str firstname ", a journey of a thousand miles begins with a single step")
        plain-subject "A journey of a thousand miles begins with a single step"]
    ;; Email to user
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject (if (some? firstname) nice-subject plain-subject)
                   :body [:alternative
                          {:type "text/plain"
                           :content welcome-message}
                          {:type "text/html"
                           :content welcome-finished}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Someone signed up!!"
                        :body (str firstname " " lastname " at email " user-email " registered for our service. Don't worry, we already emailed to tell them we love them. But maybe we should add their email to a list?")})))


(defn password-email
  "Sends a temporary password"
  [user-email password]

  (let [password-message (str "Here's your new temporary password. Remember to make yourself a new one after you log in! Your password is: " password)
        password-replacement-text [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [password-message]}]
        password-parsed (html-resource "templates/password-email.html")
        password-replaced (at password-parsed [:#whatWhat] (substitute password-replacement-text))
        password-finished (apply str (emit* password-replaced))]

    ;; Email to user
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject "Shiny new password"
                   :body [:alternative
                          {:type "text/plain"
                           :content (str password-message " Your password is: " password)}
                          {:type "text/html"
                           :content password-finished}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Password replacement"
                        :body (str user-email " generated a random password. Kind of boring, but hey.")})))

(def password-changed-message "Sent if the user email can't support HTML."
  (str "If this wasn't you, reply to this email and let us know! We'll sort it out. Otherwise, just kick back and relax."))
(def password-changed-replacement-text "HTML with the right times and days of the week."
  [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [password-changed-message]}])
(def password-changed-parsed "Turns the HTML email template into Enlive code."
  (html-resource "templates/password-changed-email.html"))
(def password-changed-replaced "Replaces the default HTML with the better HTML."
  (at password-changed-parsed [:#whatWhat] (substitute password-changed-replacement-text)))
(def password-changed-finished "Turns the finished Enlive code back into real HTML."
  (apply str (emit* password-changed-replaced)))

(defn password-changed-email
  "Notifies the user that someone has changed their password."
  [user-email]
    ;; Email to user
    (send-message conn
                  {:from "admin@expunct.com"
                   :to user-email
                   :subject "Did you change your password?"
                   :body [:alternative
                          {:type "text/plain"
                           :content password-changed-message}
                          {:type "text/html"
                           :content password-changed-finished}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Password replacement"
                        :body (str user-email " replaced their password. Kind of boring, but hey.")}))




(def payment-thanks-message "Sent if the user email can't support HTML."
  (str "Thanks for allowing us to be a part of your future! We'll get right to work on Record Recover, running a background check and pulling your file. Once we find it, we'll let you know. Remember, if you refer a friend who purchases our expunction service, we'll give you $50 off when you purchase our expunction service. You can email us at admin@expunct.com or call us at 805-538-1991 at any time."))

(def payment-thanks-replacement-text "HTML with the right times and days of the week."
  [{:type :element, :attrs {:id "onTheClock", :style "margin: 1em 0;padding: 0;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%;color: #606060;font-family: Helvetica;font-size: 15px;line-height: 150%;text-align: left;"}, :tag :p, :content [payment-thanks-message]}])
(def payment-thanks-parsed "Turns the HTML email template into Enlive code."
  (html-resource "templates/payment-thanks-email.html"))

(def payment-thanks-replaced "Replaces the default HTML with the better HTML."
  (at payment-thanks-parsed [:#whatWhat] (substitute payment-thanks-replacement-text)))
(def payment-thanks-finished "Turns the finished Enlive code back into real HTML."
  (apply str (emit* payment-thanks-replaced)))

(defn payment-thanks-email
  "Notifies the user that they've signed up."
  [email name phone birthdate arrest-date]
  (let [user-email email]
    ;; Email to user
    (send-message conn
                  {:from "example@expunct.com"
                   :to user-email
                   :subject "Good things headed your way"
                   :body [:alternative
                          {:type "text/plain"
                           :content payment-thanks-message}
                          {:type "text/html"
                           :content payment-thanks-finished}]})
    ;; Email to us
    (send-message conn {:from user-email
                        :to our-emails
                        :subject "Real money!"
                        :body (str name " actually paid us green money! 
Their email is: " user-email " and phone " phone " and birthdate " birthdate " and arrest-date " arrest-date ". Let's find their stuff!")})))


(defn help-email
  "Tells us if a server error occurred during purchase."
  [body]
;  (let [user-email (:email body)]
    ;; Email to us
    (send-message conn {:from our-emails
                        :to our-emails
                        :subject "Ack our Stripe processing had a problem"
                        :body (str body " gave us money (maybe?) but we couldn't email him or her.")}))
