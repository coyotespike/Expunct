(ns lawsuitninja.payup
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]

              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [cljs.reader :as reader]
              [lawsuitninja.components :refer [navbar
                                               text-area-output
                                               pay-button
                                               pay-wait
                                               switch-buttons
                                               back-button
                                               medium-text-box]]
              [re-frame.core :refer [register-handler
                                     path
                                     register-sub
                                     dispatch
                                     dispatch-sync
                                     subscribe]]))



(defn payme-div [showme]
  (let [logged-in? (subscribe [:logged-in?])
        address (subscribe [:your-address])
        address-string "Your address: "
        default-address? (= address address-string)]
  (fn []
       [:div
        [:div.form-centered
         [:h5 "Play fair, pay fair."]
         [:h6 "Did we miss our deadline to get back to you?"]
         [:p "Send us your address and what you were looking for."]
         [:p "If we missed your request, we'll mail you a nice green picture of Abe."]]

        [:div.hmmm
         (when-not @logged-in?
           [:p "Your name: "[medium-text-box :yourname :enter-yourname]])
         (if-not default-address?
           [:p "Your address: "[medium-text-box :your-address :enter-your-address]])
         [:p "Remind us what you wanted: "[medium-text-box :contact :enter-contact]]
         (when-not @logged-in?
           [:p "One more time: "[medium-text-box :your-email :enter-your-email]])]

        [:div.form-centered 
           (if default-address?
             [pay-wait]
             [pay-button showme])]
         [:br]
         [:br]])))

(defn honest-abe []
  (fn []
    [:div.form-centered
     [:h6 "Honest Abe. What a guy."]
      [back-button]]))

(defn payup []
  (let [showme (atom true)]
    [:div
     [:div.page-header
      [navbar]]
    [switch-buttons showme [payme-div showme] [honest-abe]]]))

