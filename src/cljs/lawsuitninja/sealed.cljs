(ns lawsuitninja.sealed
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
                                    insert-stripe
                                    public-purchase-button]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History))


(defn sealed []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
     [:h6 "What's the difference between sealing and expunging your record?"]]
     [:div.container
      [:div.col-md-4]
      [:div.col-md-8
       [:p [:b "Expunctions"]]
       [:ul
        [:li "If we can get an expunction, then the government literally shreds your case files."]
        [:li "They also delete it from their databases."]
        [:li "Private background check companies also have to delete your record."]]
       [:p [:b "Sealed, or 'ordered non-disclosed'"]]
       [:ul
        [:li "If your record can be sealed, then the government still has your case file but won't tell anyone about it."]
        [:li "This means when you apply for a job or a promotion, they can't find out about your record."]
        [:li "The police and courts can still see it though."]
        [:li "And certain fields like nursing will still be closed to you."]]
       
       [:p "The bottom line is that getting your record sealed will be a really good idea for most people, because you will have better opportunities."]
       [:p "Get started with our background check."]]]
     [:div.form-centered
      [public-purchase-button showme]]
     
     [:br]
     [:div.push]]

     [footer]

])))


(secretary/defroute "/sealed" []
  (session/put! :current-page #'sealed))
