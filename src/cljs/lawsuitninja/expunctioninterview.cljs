(ns lawsuitninja.expunctioninterview
  (:require clsjs.bootstrap)
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [re-com.core :refer [v-box radio-button h-box box]]
   [reagent-forms.core :refer [bind-fields init-field value-of]]
   [lawsuitninja.components :refer [navbar 
                                    small-text-box
                                    switch-buttons
                                    medium-text-box
                                    text-area-input
                                    wide-text-box
                                    alt-picker
                                    footer
                                    popover]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History))

(defn change-page-button [page] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-alizarin" 
           :value "Got it!"
           :on-click #(set! (.-hash js/window.location) page)}]))


(defn interview []
  [:div.Site
   [:div.wrapper
   [:div.page-header
  [navbar]]
   [:div.form-centered
       [:h4 "As we get started..."]
    [:p "This part of our website will give you more information about whether your record can be cleared."
     [:br]
     [:br]
     "If you're not sure what to choose, just pick 'not sure'."
     [:br] 
     [:br] [:br]
     [change-page-button "/expungeable"]
]]]
     [footer]
])

(secretary/defroute "/interview" []
  (session/put! :current-page #'interview))

