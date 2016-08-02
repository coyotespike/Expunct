(ns lawsuitninja.sub
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [clairvoyant.core :as trace :include-macros true]))

(register-sub
 :state
 (fn [db]
   (reaction (:state @db))))

(register-sub
 :typeahead
 (fn [db]
   (reaction (:typeahead @db))))

(register-sub
 :related-charges
 (fn [db]
   (reaction (:related-charges @db))))

(register-sub
 :expunction-purchase
 (fn [db]
   (reaction (:expunction-purchase @db))))

(register-sub
 :background-check-purchase
 (fn [db]
   (reaction (:background-check-purchase @db))))


(register-sub
 :case-disposition
 (fn [db]
   (reaction (:case-disposition @db))))
(register-sub
 :dismissal-date
 (fn [db]
   (reaction (:dismissal-date @db))))

(register-sub
 :arrest-county
 (fn [db]
   (reaction (:arrest-county @db))))

(register-sub
 :arrest-date
 (fn [db]
   (reaction (:arrest-date @db))))

(register-sub
 :birthdate
 (fn [db]
   (reaction (:birthdate @db))))


(register-sub
 :first-name
 (fn [db]
   (reaction (:first-name @db))))

(register-sub
 :last-name
 (fn [db]
   (reaction (:last-name @db))))

(register-sub
   :logged-in?
   (fn [db]
     (reaction (:logged-in? @db))))

(register-sub
   :your-email
   (fn [db]
     (reaction (:your-email @db))))

(register-sub
   :contact
   (fn [db]
     (reaction (:contact @db))))

(register-sub
   :yourname
   (fn [db]
     (reaction (:yourname @db))))
(register-sub
   :your-signature
   (fn [db]
     (reaction (:your-signature @db))))
(register-sub
   :your-printed-name
   (fn [db]
     (reaction (:your-printed-name @db))))   ;; pulls out :name
(register-sub        ;; a new subscription handler
   :your-address             ;; usage (subscribe [:name])
   (fn [db]
     (reaction (:your-address @db))))   ;; pulls out :name
(register-sub        ;; a new subscription handler
   :city-state             ;; usage (subscribe [:name])
   (fn [db]
     (reaction (:city-state @db))))   ;; pulls out :name
(register-sub        ;; a new subscription handler
   :your-phone             ;; usage (subscribe [:name])
   (fn [db]
     (reaction (:your-phone @db))))   ;; pulls out :name

(register-sub
 :app-db
 (fn [db]
   db))
