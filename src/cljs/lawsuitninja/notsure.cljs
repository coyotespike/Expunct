(ns lawsuitninja.notsure
    (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [re-com.core :refer [v-box radio-button]]
   [lawsuitninja.components :refer [navbar 
                                    footer
                                    small-text-box
                                    switch-buttons
                                    change-page-button
                                    medium-text-box
                                    text-area-input
                                    wide-text-box
                                    alt-picker
                                    typeahead
;                                    save-button
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


(defn unsure []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div.Site
     [:div.wrapper
      [:div.page-header
       [navbar]]   
      [:div.form-centered
       [:h6 "That's okay."]
       [:p "If you're not sure what happened with your case, we'll find it and take a look."]
       [:p "Get started with Record Recover""\u2122" ". " ]
       [public-purchase-button showme]]
      [:br]]
;      [:div.push]

     [footer]
])))

(secretary/defroute "/notsure" []
  (session/put! :current-page #'unsure))
