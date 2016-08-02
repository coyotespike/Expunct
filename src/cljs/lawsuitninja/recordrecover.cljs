(ns lawsuitninja.recordrecover
    (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [cljsjs.react :as react]
   [cljs.reader :as reader]
   [lawsuitninja.components :refer [navbar 
                                    small-text-box 
                                    insert-stripe
                                    public-purchase-button
                                    payment-information
                                    footer
                                    linky-button]]
   [lawsuitninja.termsandconditions :refer [privacy-modal tc-modal]]
   [reagent-modals.modals :as reagent-modals]
   [lawsuitninja.datareview :refer [data-review
                                    edit-info]]
   [lawsuitninja.register-page :refer [finish-register]]
   [goog.dom.classlist :as classlist]
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



(defn recordrecover []
;  (insert-stripe)
  (let [showme (atom "a")]
    (fn []
  [:div.Site
   [:div.page-header
    [navbar]]
   [:div.wrapper
    [:div.form-centered
     [:h5 "Welcome!"]
     [:p "Get started with a free background check."]
     [:p "We'll see if we can find you in the Texas database and email you with what we find."]
     [:p "This tells us if we can file for you."]

     [:div.form-centered
      [:p "By clicking Register, you agree to the "
       [linky-button "terms and conditions " #(reagent-modals/modal! [tc-modal])]
       "and the "
       [linky-button " privacy policy." #(reagent-modals/modal! [privacy-modal])]]]


     [public-purchase-button showme]]]
   [footer]
   [reagent-modals/modal-window]
    ])))

(secretary/defroute "/recordrecover" []
  (session/put! :current-page #'recordrecover))
