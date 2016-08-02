(ns lawsuitninja.payments
  (:require 
   [clj-stripe.common :as common]
   [clj-stripe.charges :as charges]
   [clj-stripe.cards :as cards]
   [clj-stripe.customers :as customers]
   [lawsuitninja.emails :refer [payment-thanks-email
                                help-email]]))


;; test key
;(def secret-key "sk_test_uIKktrJGxKFY3e3QWjs4sKHn")

(defn charge-card [customer email amount name phone birthdate arrest-date]        
  (let [charge
        (common/with-token secret-key
          (common/execute 
           (charges/create-charge 
            (common/money-quantity amount "usd") 
            (common/customer (:id customer))
            (common/description "Your background check is on the way!"))))
        success (nil? (:error charge))
        error (:message (:error charge))
        message (:description charge)]
    (if success
      (do
        (try
          (payment-thanks-email email name phone birthdate arrest-date)
          (catch Exception ex ;java.lang.NullPointerException
              (help-email customer)))
         {:status 200 :body message})
      {:status 402 :body error})))


(defn create-customer [req]
  (let [token (get-in req [:body :id])
        email (get-in req [:body :email])
        amount (get-in req [:body :amount])
        name (get-in req [:body :name])
        phone (get-in req [:body :phone])
        birthdate (get-in req [:body :birthdate])
        arrest-date (get-in req [:body :arrest-date])
        customer
        (common/with-token secret-key
          (common/execute 
           (customers/create-customer 
            (common/card token)
            (customers/email email))))
        success (nil? (:error customer))
        error (:message (:error customer))]
    (if success
      (charge-card customer email amount name phone birthdate arrest-date)
      {:status 402 :body error})))
    

   


