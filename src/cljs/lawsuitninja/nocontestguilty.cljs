(ns lawsuitninja.nocontestguilty
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [re-com.core :refer [v-box radio-button h-box box]]
   [lawsuitninja.components :refer [navbar 
                                    small-text-box
                                    footer
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

(defmethod change-page "Cmisdemeanor" []
  (set! (.-hash js/window.location) "/classC"))
(defmethod change-page "ABmisdemeanor" []
  (set! (.-hash js/window.location) "/ABFelony"))
(defmethod change-page "felony" []
  (set! (.-hash js/window.location) "/ABFelony"))
(defmethod change-page "unsure" []
  (set! (.-hash js/window.location) "/notsure"))



(def ABmisdemeanor "I was convicted of a Class A or Class B misdemeanor.")
(def Cmisdemeanor "I was convicted of a Class C misdemeanor.")
(def felony "I was convicted of a state or federal felony.")
(def not-sure "I think I have a record, but I'm not sure for what.")



(defn nocontestguilty []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h5 "We're making progress."]
      [:p "Please give us a little more information."]
      [:p "Even if you entered a plea to avoid jail or to receive an easier sentence, you still got a conviction."]]
       [:div.container
        [:div.row
         [:div.col-xs-4.col-sm-4.col-md-6.col-lg-6
          [v-box
           :width "300px"
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
                                      (dispatch [:enter-case-disposition "AB conviction"])
                                     (reset! description ABmisdemeanor))]

                      [radio-button 
                       :label "Class C Misdemeanor"
                       :value "Cmisdemeanor"
                       :model model
                       :on-change #(do
                                     (reset! model "Cmisdemeanor")
                                      (dispatch [:enter-case-disposition "C conviction"])
                                     (reset! description Cmisdemeanor))]

                      [radio-button 
                       :label "Felony conviction"
                       :value "felony"
                       :model model
                       :on-change #(do
                                     (reset! model "felony")
                                      (dispatch [:enter-case-disposition "felony conviction"])
                                     (reset! description felony))]
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
     [:br]]

     [footer]
       ])))


(secretary/defroute "/nocontestguilty" []
  (session/put! :current-page #'nocontestguilty))
