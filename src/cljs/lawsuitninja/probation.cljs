(ns lawsuitninja.probation
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
                                    popover]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History))



(defmulti change-page (fn [model] model))
(defmethod change-page "straight" []
  (set! (.-hash js/window.location) "/nonexpungeable"))
(defmethod change-page "deferred" []
  (set! (.-hash js/window.location) "/sealed"))

(defmethod change-page "pretrial" []
  (set! (.-hash js/window.location) "/dismissed"))
(defmethod change-page "defpros" []
  (set! (.-hash js/window.location) "/dismissed"))
(defmethod change-page "defdis" []
  (set! (.-hash js/window.location) "/dismissed"))

(defmethod change-page "unsure" []
  (set! (.-hash js/window.location) "/notsure"))



(def straight "This means you were convicted. Unless it was a Class C Misdemeanor, it cannot be expunged.")
(def deferred "Although you didn’t ultimately get a conviction, you can't get this record expunged, but you can get it sealed.")
(def pretrial "You didn’t get a conviction, and this can be expunged.")
(def not-sure "I think I have a record, but I'm not sure for what.")

(defn probation []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
  (fn []
    [:div
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h5 "Okay, let's explain a couple of things."]
      ]
      [:div
       [:div.container
        [:div.col-md-4]
      [:div.col-md-8
       [:p "In Texas, probation is technically called community supervision. If you’ve heard either of those terms, they mean the same thing."]
       [:p "Here are some options - which of them sounds most like your case?"]]]]

       [:div.container
        [:div.row
         [:div.col-xs-4.col-sm-4.col-md-6.col-lg-6
          [v-box
;           :width "300px"
           :children [[:p @description]]]]
         [:div.col-xs-8.col-sm-8.col-md-4.col-lg-3.col-lg-offset-3
          [v-box
           :children [
                      [radio-button 
                       :label "Conviction with straight probation"
                       :value "straight"
                       :model model
                       :on-change #(do
                                     (reset! model "straight")
                                      (dispatch [:enter-case-disposition @model])
                                     (reset! description straight))]

                      [radio-button 
                       :label "Deferred adjudication with probation."
                       :value "deferred"
                       :model model
                       :on-change #(do
                                     (reset! model "deferred")
                                      (dispatch [:enter-case-disposition @model])
                                     (reset! description deferred))]

                      [:br]
                      [radio-button 
                       :label "Pretrial Diversion"
                       :value "pretrial"
                       :model model
                       :on-change #(do
                                     (reset! model "pretrial")
                                      (dispatch [:enter-case-disposition @model])
                                     (reset! description pretrial))]

                      [radio-button 
                       :label "Deferred Prosecution"
                       :value "defpros"
                       :model model
                       :on-change #(do
                                     (reset! model "defpros")
                                      (dispatch [:enter-case-disposition @model])
                                     (reset! description pretrial))]

                      [radio-button 
                       :label "Deferred Disposition with probation"
                       :value "defdis"
                       :model model
                       :on-change #(do
                                     (reset! model "defdis")
                                      (dispatch [:enter-case-disposition @model])
                                     (reset! description pretrial))]

                      [:br]
                      [radio-button 
                       :label "Not sure?"
                       :value "unsure"
                       :model model
                       :on-change #(do
                                     (reset! model "unsure")
                                      (dispatch [:enter-case-disposition @model])
                                     (reset! description not-sure))]


                      ]]]]]
     [:div.form-centered
        [:input {:class "btn btn-info palette-turquoise"
                 :type "button"
                 :value "This one"
                 :on-click #(change-page @model)}]]
      [:br]
     [:div.push]
]
     [footer]
])))


(secretary/defroute "/probation" []
  (session/put! :current-page #'probation))
