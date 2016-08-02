(ns lawsuitninja.reset-password
  (:require   [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [cljs.reader :as reader]
              [re-frame.core :refer [register-handler
                                     path
                                     register-sub
                                     dispatch
                                     dispatch-sync
                                     subscribe]]
              [lawsuitninja.components :refer [navbar
                                               new-password-button]])

    (:require-macros
     [reagent.ratom :refer [reaction]]))

(defn reset-password []
  [:div
   [:div.page-header
    [navbar]]
   [:div.form-centered
   [:h5 "No sweat."]
   [:p "Confirm that you want to reset your password by pushing the button below."]
   [:p "We'll email you a temporary password and take you back to the login page."]
    [:p "(we can't send your current password, because we don't know it.)"]
   [:p "After you use the temporary password, you can make a new one in your profile."]
    [new-password-button]]])

(secretary/defroute "/reset-password" []
  (session/put! :current-page #'reset-password))
