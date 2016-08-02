(ns lawsuitninja.dismissed
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
                                    days-between
                                    popover
                                    insert-stripe
                                    public-purchase-button]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History)
  (:require-macros [reagent.ratom :refer [reaction]]))

(def today (js/Date.))
(def total-days-selected (reaction (days-between @(subscribe [:dismissal-date]) today)))

(defmulti change-page (fn [model] model))

(defmethod change-page "Cmisdemeanor" []
  (set! (.-hash js/window.location) "/ClassCdismissed"))

(defmethod change-page "ABmisdemeanor" []
  (set! (.-hash js/window.location) "/ClassABdismissed"))
(defmethod change-page "felony" []
  (set! (.-hash js/window.location) "/felonydismissed"))

(defmethod change-page "conviction" []
  (set! (.-hash js/window.location) "/nonexpungeable"))

(defmethod change-page "unsure" []
  (set! (.-hash js/window.location) "/notsure"))


(def ABmisdemeanor "I was charged with a Class A or Class B misdemeanor.")
(def Cmisdemeanor "I was charged with  a Class C misdemeanor.")
(def felony "I was charged with a state or federal felony.")

(def not-sure "I think I have a record, but I'm not sure for what.")
(def not-here "I was arrested, and pled guilty or was convicted.")

(defn C-waiting []
  (fn []
    [:div
    (cond
      (<= 180 @total-days-selected) [:i "That's more than 180 days, so you can get an expunction right now."]
      (> 180 @total-days-selected 0) [:i "You can't get an expunction until 180 days have passed, but we can get started drafting your petition."]
      :else [:i "Tell us when your case was dismissed."])]))

(defn AB-waiting []
  (fn []
    [:div
    (cond
      (<= 365 @total-days-selected) [:i "That's more than a year ago, so you can get an expunction right now."]
      (> 365 @total-days-selected 0) [:i "You can't get an expunction until a year has passed, but we can get started drafting your petition."]
      :else [:i "Tell us when your case was dismissed."])]))

(defn felony-waiting []
  (fn []
    [:div
    (cond
      (<= 1095 @total-days-selected) [:i "That's more than three years ago, so you can get an expunction right now."]
      (> 1095 @total-days-selected 0) [:i "You can't get an expunction until three years have passed, but we can get started drafting your petition."]
      :else [:i "Tell us when your case was dismissed."])]))

(defn C-charge []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h6 "Tell us when your case was dismissed."]]
     [:br]
     [:div.container
     [:div.col-md-8.col-centered
      [:form.form-horizontal {:role "form"}

       [:div.form-group
        [:div.col-sm-6 [alt-picker :dismissal-date]]
        [:div.col-sm-6 
         [C-waiting]]]]]]
     [:br]
     [:div.form-centered
      [:p "When you're ready, you can get started with Record Recover."]
      [public-purchase-button showme]]
      [:br]]

     [footer]

])))

(defn AB-charge []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h6 "Tell us when your case was dismissed."]]
     [:br]
     [:div.container
     [:div.col-md-8.col-centered
      [:form.form-horizontal {:role "form"}

       [:div.form-group
        [:div.col-sm-6 [alt-picker :dismissal-date]]
        [:div.col-sm-6 [AB-waiting]]]]]]
     [:br]
     [:div.form-centered
      [:p "When you're ready, you can get started with Record Recover."]
      [public-purchase-button showme]]
     [:br]
     [:br]]
     [footer]
])))

(defn felony-charge []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h6 "Tell us when your case was dismissed."]]
     [:br]
     [:div.container
     [:div.col-md-8.col-centered
      [:form.form-horizontal {:role "form"}
       [:div.form-group
        [:div.col-sm-6 [alt-picker :dismissal-date]]
        [:div.col-sm-6 [felony-waiting]]]]]]
     [:br]
     [:div.form-centered
      [:p "When you're ready, you can get started with Record Recover."]
      [public-purchase-button showme]]

          [:br]
     [:div.push]]

     [footer]

])))
  

(defn case-dismissed []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered

      [:h5 "Great! If your case was dismissed, we can get an expunction for you."]
      [:p "However, we need a little more information."]
      [:p "Different charges have different waiting periods."]]
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
                                     (dispatch [:enter-case-disposition "Class C conviction"])
                                     (reset! description Cmisdemeanor))]

                      [radio-button 
                       :label "Felony charge"
                       :value "felony"
                       :model model
                       :on-change #(do
                                     (reset! model "felony")
                                     (dispatch [:enter-case-disposition "felony conviction"])
                                     (reset! description felony))]


                      [:br]
                      [radio-button 
                       :label "None of these"
                       :value "conviction"
                       :model model
                       :on-change #(do
                                     (reset! model "conviction")
                                      (dispatch [:enter-case-disposition "not in conviction page"])
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
     [footer]

       ])))



(secretary/defroute "/dismissed" []
  (session/put! :current-page #'case-dismissed))

(secretary/defroute "/ClassCdismissed" []
  (session/put! :current-page #'C-charge))

(secretary/defroute "/ClassABdismissed" []
  (session/put! :current-page #'AB-charge))

(secretary/defroute "/felonydismissed" []
  (session/put! :current-page #'felony-charge))
