(ns lawsuitninja.ABFelony
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [reagent-modals.modals :as reagent-modals]
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
                                    days-between]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History)
  (:require-macros [reagent.ratom :refer [reaction]]))


(defn ABFelony []
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h5 "We're very sorry"]
      [:p "If you were convicted of a Class A or Class B misdemeanor, or a felony, your record cannot be expunged."]
      [:p "Even if you were a juvenile at the time, you cannot get the record expunged."]
      
      [:p "However, you may still be able to have a different record expunged."]
      [:p "Click below to start the interview over."]
      [change-page-button "/expungeable"]]
     [:br]
]

    [footer]

    ]))


(secretary/defroute "/ABFelony" []
  (session/put! :current-page #'ABFelony))

