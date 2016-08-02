(ns lawsuitninja.identitytheft
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [re-com.core :refer [v-box radio-button h-box box]]
   [lawsuitninja.components :refer [navbar 
                                    small-text-box
                                    switch-buttons
                                    change-page-button
                                    medium-text-box
                                    text-area-input
                                    wide-text-box
                                    alt-picker
                                    blue-swap-button
                                    popover
                                    days-between
                                    insert-stripe
                                    public-purchase-button]]

   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History)
  (:require-macros [reagent.ratom :refer [reaction]]))

(def today (js/Date.))
(def total-days-selected (reaction (days-between @(subscribe [:arrest-date]) today)))

(defn arrest-label []
  (fn []
    [:div
    (cond
      (<= 60 @total-days-selected) [:i "That's more than 60 days, so you can get an expunction right now."]
      (> 60 @total-days-selected 0) [:i "You can't get an expunction until 60 days have passed, but we can get started drafting your petition."]
      :else [:i "What date is on the record?"])]))

(defn identitytheft []
  (insert-stripe)
  (let [showme (atom "a")]

  (fn []
    [:div
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h5 "Okay, we can handle this."]
      [:p "If you were the victim of identity theft or confusion, you can absolutely get your record cleared."]
      [:p "The important thing is that you were never charged or convicted."]
      [:br]
      [:p "If that's the case, then click below to have us search for the incorrect records and tell you what we find."]

      [public-purchase-button showme]]
     [:br]
     ])))

(secretary/defroute "/identitytheft" []
  (session/put! :current-page #'identitytheft))
