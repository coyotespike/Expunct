(ns lawsuitninja.nocharge
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [re-com.core :refer [v-box radio-button h-box box]]
   [lawsuitninja.components :refer [navbar 
                                    footer
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
      :else [:i "Tell us when you were arrested"])]))

(defn nocharge []
  (insert-stripe)
  (let [showme (atom "a")]

  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h5 "Good, this is the best possible case."]
      [:h6 "Tell us when you got arrested."]]
     [:br]
     [:div.container
     [:div.col-md-8.col-centered
      [:form.form-horizontal {:role "form"}

       [:div.form-group
        [:div.col-sm-6 [alt-picker :arrest-date]]
        [:div.col-sm-6 [arrest-label]]]]]]
     [:br]
     [:div.form-centered
      [:p "When you're ready, you can get started with our background check."]
      [public-purchase-button showme]]
      [:br]
      [:div.push]]

     [footer]

])))

(secretary/defroute "/nocharge" []
  (session/put! :current-page #'nocharge))

