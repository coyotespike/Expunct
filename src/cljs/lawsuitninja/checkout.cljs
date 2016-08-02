(ns lawsuitninja.checkout
    (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [cljsjs.react :as react]
   [cljs.reader :as reader]
   [cljs.core.async :as async]
   [lawsuitninja.components :refer [navbar small-text-box footer]]
   [reagent-modals.modals :as reagent-modals]
   [lawsuitninja.db :refer [update-us]]
   [lawsuitninja.components :refer [purchase-button
                                    insert-stripe]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [cljs.core.async :as async :refer [put! <! >! chan]])
  (:import goog.History)
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]]))



(defn checkout-page []
  (insert-stripe)
  [:div.Site
   [:div.wrapper
    [:div.page-header
     [navbar]]
    [:div.centered-tabs
     [:ul {:class "nav nav-tabs" :role "tablist"}
      [:li {:role "presentation" :class "active"} [:a {:href "#background-check" :aria-controls "background-check" :role "tab" :data-toggle "tab"} "Record Recover"]]
;      [:li {:role "presentation"} [:a {:href "#expunction" :aria-controls "expunction" :role "tab" :data-toggle "tab"} "Expunction or Nondisclosure"]]
]
     [:div {:class "tab-content"}
      [:div {:role "tabpanel" :class "tab-pane active fade in" :id "background-check"}
        [:div.form-centered 
         [:h4 "Let's get the ball rolling"]
         [:p "Thanks for trusting us to help you reach a better future. We're excited to get to work."]
         [:p "After you fill out your card details, we'll charge $25 now, and contact you when we have your case file."]
         [:p "Once we've confirmed your case can be expunged, we'll be ready to take the next step."]
         [:br]
         [purchase-button 2500]]]

      ;; [:div {:role "tabpanel" :class "tab-pane fade in" :id "expunction"}
       
      ;;  [:div.form-centered 
      ;;   [:h4 "Let's wrap this up"]
      ;;   [:p "We've got your case file and we're ready to help you clear your record."]
      ;;   [:p "Once again, thanks for trusting us to help you reach a better future. Let's take the next step."]
      ;;   [:p "After you fill out your card details, we'll charge $400 now, and contact you when we're notified of court costs."]
      ;;   [:br]
      ;;   [purchase-button 40000]]]
]
]]
   [footer]
   [reagent-modals/modal-window]])


(secretary/defroute "/checkout" []
  (session/put! :current-page #'checkout-page))
