(ns clj.lawsuitninja.test.stripetest
  (:use clojure.test)
  (:use [lawsuitninja.payments])
  (:require
   [clj-stripe.common :as common]
   [clj-stripe.cards :as cards]))


(defn card-token [number]
  (:id
   (common/with-token secret-key
     (common/execute
      (cards/create-card-token  
       (common/card 
        (common/number number)
        (common/expiration 12 2017) 
        (common/cvc 123)))))))

(def expired-card (card-token "4000000000000069"))
(def no-charge-customer (card-token 4000000000000341))
(def bad-security-code-card (card-token 4000000000000127))
(def declined-card (card-token 4000000000000002))
(def declined-fraud-card (card-token 4100000000000019))
(def successful-charge (card-token 4000000000000077))
(def processing-error (card-token 4000000000000119))


(deftest Stripe-checks
  "Checking error codes from creating a customer"  

  (testing "message from a declined card"
    (is (= "Your card was declined."
           (:body 
            (create-customer {:body {:id declined-card}})))))

  (testing "message for an incorrect security code."
    (is (= "Your card's security code is incorrect."
           (:body 
            (create-customer {:body {:id bad-security-code-card}})))))

  (testing "expired card message."
    (is (= "Your card has expired."
           (:body 
            (create-customer {:body {:id expired-card}})))))

  )

(deftest Charge-card
  "Checking error codes from charging the card"

  (testing "Declined due to possible fraud."
    (is (= "Your card was declined."
           (:body 
            (create-customer {:body {:id declined-fraud-card}})))))

  (testing "Customer created but card cannot be charged."
    (is (= "Your card was declined."
           (:body 
            (create-customer {:body {:id no-charge-customer}})))))

  (testing "A successful charge!"
    (is (= "Your background check is on the way!"
           (:body 
            (create-customer {:body {:id successful-charge}})))))

  (testing "A processing error during charging"
    (is (= "An error occurred while processing your card. Try again in a little bit."
           (:body 
            (create-customer {:body {:id processing-error}})))))

  )
