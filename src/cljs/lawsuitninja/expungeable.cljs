(ns lawsuitninja.expungeable
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
                                    change-page-button
                                    medium-text-box
                                    text-area-input
                                    wide-text-box
                                    alt-picker
                                    footer
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
(defmethod change-page "dismissed" []
  (set! (.-hash js/window.location) "/dismissed"))

(defmethod change-page "conviction" []
  (set! (.-hash js/window.location) "/nonexpungeable"))

(defmethod change-page "no-charge" []
  (set! (.-hash js/window.location) "/nocharge"))

(defmethod change-page "identity" []
  (set! (.-hash js/window.location) "/identitytheft"))

(defmethod change-page "acquittal" []
  (set! (.-hash js/window.location) "/acquittal"))

(defmethod change-page "community" []
  (set! (.-hash js/window.location) "/probation"))

(defmethod change-page "unsure" []
  (set! (.-hash js/window.location) "/notsure"))



(def case-dismissed "I went to court, and my case was dismissed. I was given deferred prosecution or pretrial diversion.")
(def no-charge "I was arrested, but the police never charged me with a crime. I never went to court.")
(def no-bill "The police charged me and the prosecutor brought a case, but the grand jury returned a no-bill.")
(def acquittal "We went to a criminal trial, but I was acquitted by the judge or jury.")
(def identity-theft "I wasn't arrested, but I have a record. They say someone might have given my name.")
(def pardon "I was convicted, but the governor issued me a pardon.")
(def not-sure "I think I have a record, but I'm not what happened to my case.")
(def not-here "None of these sound quite right.")
(def community "I received probation, or community supervision.")


(defn expungeable-offenses []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
    (fn []
      [:div.Site
       [:div.wrapper
        [:div.page-header
         [navbar]]
        [:div.form-centered
         [:h5 "Please choose what happened to your case."]]
        [:div.container
         [:div.row
          [:div.col-xs-4.col-sm-4.col-md-6.col-lg-6
           [v-box
            :children [[:p @description]]]]
          [:div.col-xs-8.col-sm-8.col-md-5.col-lg-4;.col-lg-offset-3
           [v-box
            :children [
                       [radio-button 
                        :label "Case Dismissed"
                        :value "dismissed"
                        :model model
                        :on-change #(do
                                      (reset! model "dismissed")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description case-dismissed))]
                       [radio-button 
                        :label "Arrest, but no charge"
                        :value "no-charge"
                        :model model
                        :on-change #(do
                                      (reset! model "no-charge")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description no-charge))]
                       [radio-button 
                        :label "Probation (community supervision)"
                        :value "community"
                        :model model
                        :on-change #(do
                                      (reset! model "community")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description community))]

                       ;; [radio-button 
                       ;;  :label "No-billed by grand jury"
                       ;;  :value "no-bill"
                       ;;  :model model
                       ;;  :on-change #(do
                       ;;                (reset! model "no-bill")
                       ;;                (reset! description no-bill))]

                       [radio-button 
                        :label "Acquittal"
                        :value "acquittal"
                        :model model
                        :on-change #(do
                                      (reset! model "acquittal")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description acquittal))]

                       [radio-button 
                        :label "Identity Theft"
                        :value "identity"
                        :model model
                        :on-change #(do
                                      (reset! model "identity")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description identity-theft))]

                       ;; [radio-button 
                       ;;  :label "Governor's Pardon"
                       ;;  :value "pardon"
                       ;;  :model model
                       ;;  :on-change #(do
                       ;;                (reset! model "pardon")
                       ;;                (reset! description pardon))]
                       
                       [:br]
                       [radio-button 
                        :label "Don't see it?"
                        :value "conviction"
                        :model model
                        :on-change #(do
                                      (reset! model "conviction")
                                      (dispatch [:enter-case-disposition "didn't see it"])
                                      (reset! description not-here))]

                       [radio-button 
                        :label "Not sure?"
                        :value "unsure"
                        :model model
                        :on-change #(do
                                      (reset! model "unsure")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description not-sure))]

                        ]]]]]
                        [:br]
                        [:div.form-centered
                         [:input {:class "btn btn-info palette-turquoise"
                                  :type "button"
                                  :value "This one"
                                  :on-click #(change-page @model)}]]
                        [:br]]
             [footer]])))


(secretary/defroute "/expungeable" []
  (session/put! :current-page #'expungeable-offenses))

