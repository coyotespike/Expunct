(ns lawsuitninja.nonexpungeable
  (:require clsjs.bootstrap)
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
                                    typeahead
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

(defmethod change-page "pastprobation" []
  (set! (.-hash js/window.location) "/probation"))
(defmethod change-page "currentprobation" []
  (set! (.-hash js/window.location) "/probation"))
(defmethod change-page "deferredadjudication" []
  (set! (.-hash js/window.location) "/probation"))

(defmethod change-page "Cmisdemeanor" []
  (set! (.-hash js/window.location) "/classC"))

(defmethod change-page "ABmisdemeanor" []
  (set! (.-hash js/window.location) "/ABFelony"))
(defmethod change-page "felony" []
  (set! (.-hash js/window.location) "/ABFelony"))

(defmethod change-page "nocontest" []
  (set! (.-hash js/window.location) "/nocontestguilty"))

(defmethod change-page "guiltyplea" []
  (set! (.-hash js/window.location) "/nocontestguilty"))
(defmethod change-page "unsure" []
  (set! (.-hash js/window.location) "/notsure"))



(def pastprobation "I was convicted and got probation, and I've completed my probation.")
(def ABmisdemeanor "I was convicted of a Class A or Class B misdemeanor.")
(def Cmisdemeanor "I was convicted of a Class C misdemeanor.")
(def guiltyplea "I pled guilty to the offense the government charged me with.")
(def nocontest "I entered a plea of no contest.")
(def deferredadjudication "As long as I don't commit another crime, the adjudication for this charge was deferred.")
(def currentprobation "I'm on probation right now.")
(def felony "I was convicted of a state or federal felony.")

(def not-sure "I think I have a record, but I'm not sure for what.")



(defn nonexpungeable []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
    (fn []
      [:div.Site
       [:div.wrapper
        [:div.page-header
         [navbar]]
        [:div.form-centered
         [:h5 "Do any of these sound right?"]]

        [:div.container
         [:div.row
          [:div.col-xs-4.col-sm-4.col-md-6.col-lg-6
           [v-box
            :children [[:p @description]]]]
          [:div.col-xs-8.col-sm-8.col-md-4.col-lg-3.col-lg-offset-3
           [v-box
            :children [
                       [radio-button 
                        :label "Class A or B Misdemeanor"
                        :value "ABmisdemeanor"
                        :model model
                        :on-change #(do
                                      (reset! model "ABmisdemeanor")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description ABmisdemeanor))]

                       [radio-button 
                        :label "Class C Misdemeanor"
                        :value "Cmisdemeanor"
                        :model model
                        :on-change #(do
                                      (reset! model "Cmisdemeanor")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description Cmisdemeanor))]

                       [radio-button 
                        :label "Felony conviction"
                        :value "felony"
                        :model model
                        :on-change #(do
                                      (reset! model "felony")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description felony))]

                       [radio-button 
                        :label "Probation - discharged"
                        :value "pastprobation"
                        :model model
                        :on-change #(do
                                      (reset! model "pastprobation")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description pastprobation))]
                       [radio-button 
                        :label "Probation - current"
                        :value "currentprobation"
                        :model model
                        :on-change #(do
                                      (reset! model "currentprobation")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description currentprobation))]

                       [radio-button 
                        :label "Guilty Plea"
                        :value "guiltyplea"
                        :model model
                        :on-change #(do
                                      (reset! model "guiltyplea")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description guiltyplea))]

                       [radio-button 
                        :label "No-contest plea"
                        :value "nocontest"
                        :model model
                        :on-change #(do
                                      (reset! model "nocontest")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description nocontest))]

                       [radio-button 
                        :label "Deferred adjudication"
                        :value "deferredadjudication"
                        :model model
                        :on-change #(do
                                      (reset! model "deferredadjudication")
                                      (dispatch [:enter-case-disposition @model])
                                      (reset! description deferredadjudication))]

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
        [:br]

        [:div.form-centered
         [:input {:class "btn btn-info palette-turquoise"
                  :type "button"
                  :value "This one"
                  :on-click #(change-page @model)}]]
        ]

       [footer]
       ])))

(secretary/defroute "/nonexpungeable" []
  (session/put! :current-page #'nonexpungeable))


