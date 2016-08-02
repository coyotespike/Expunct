(ns lawsuitninja.contact
  (:require [reagent.core :as reagent :refer [atom render-component]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [cljs.reader :as reader]
            [dommy.core :refer-macros [sel1]]
            [dommy.core :as dommy]            
            [clojure.string :as s]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [lawsuitninja.components :refer [navbar
                                             footer
                                             text-area-output
                                             wide-text-box
                                             medium-text-box
                                             small-text-box
                                             wait-button
                                             back-button
                                             email-button
                                             switch-buttons]])
  (:import goog.History)
  (:require-macros
   [reagent.ratom :refer [reaction]]))


;; Regex for a valid email address
(def email-regex #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")

(defn valid?
  "Determine if the specified email address is valid according to our email regex."
  [email]
  (if (and (not (nil? email)) (re-matches email-regex email))
  true 
  false))

(defn thank-you-div []
  (fn []
  [:div.form-centered
   [:div
    [:h4 "Awesome work!"]
    [:p "Thanks for helping us help you - we'll be in touch shortly!"]
    [back-button]]]))

(defn info-div [showme]
  (fn []
  [:div
   [:div.form-centered
    [:h4 "We'll answer your question in 24 hrs or give you $5."]
    [:p "Tell us what you're looking for. We'll get back to you within 24 hours, or mail you a $5 bill."]
    [:p "(Small print: we can give legal information, not legal advice.)"]]
   [:div.hmmm
    [text-area-output :contact :enter-contact]
    [medium-text-box :your-email :enter-your-email]
     [:br]
     [:br]
    [:div.form-centered 
     (let [emailz (subscribe [:your-email])]
       (if (valid? (s/lower-case @emailz))
         [email-button showme]
         [wait-button]))]]]))


(defn contact-info []
  (let [showme (atom true)]
  [:div
   [:div.wrapper
   [:div.page-header
    [navbar]]

   [switch-buttons showme [info-div showme] [thank-you-div]]
   [:br]
   [:div.form-centered [:a {:href "#/payup"} "Do we need to pay up?"]]
     [:br]
     [:div.push]]
     [footer]
]))
