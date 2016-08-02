(ns lawsuitninja.datareview
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
                                    email-input
;                                    save-button
                                    card-button
                                    password-form
                                    blue-swap-button
                                    popover]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History))


(defn save-button [showme] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Save"
           :on-click #(do
                        (.-preventDefault (swap! showme not))
                        (update-us))}]))


(defn edit-info [showme]
  (let [nameclass (atom "")
        phoneclass (atom "")
        emailclass (atom "")
        birthclass (atom "")
        adateclass (atom "")
        countyclass (atom "")]
    (fn []
  [:div
   [:div.modal-header
    [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
    [:h6.modal-title "We need some details to finish your purchase."]]
   [:div.modal-body
    [:form.form-horizontal {:role "form"}
     
      [:div.form-group
       [:div#legalname.col-xs-8 {:class @nameclass} [medium-text-box :yourname :enter-yourname]]
       [:label.col-xs-4 {:for "legalname"} [:i "Name "]]]

      [:div.form-group
       [:div#phone.col-xs-8 {:class @phoneclass} [medium-text-box :your-phone :enter-your-phone]]
       [:label.col-xs-4 [:i "Phone"]]]

      [:div.form-group
       [:div#email.col-xs-8 {:class @emailclass} [medium-text-box :your-email :enter-your-email]]
       [:label.col-xs-4 {:for "email"} [:i "Your email "]]]

      [:div.form-group
       [:div#birthdate.col-xs-8 {:class @birthclass} [alt-picker :birthdate]]
       [:label.col-xs-4 {:for "birthdate"} [:i "Birthdate"]]]

      [:div.form-group
       [:div#arrest-date.col-xs-8 {:class @adateclass} [alt-picker :arrest-date]]
       [:label.col-xs-4 {:for "arrest-date"} [:i "Arrest date"]]]

      [:div.form-group
       [:div#arrest.col-xs-8 {:class @countyclass}
        [typeahead]]
       [:label.col-xs-4 {:for "arrest"} [:i "Location"]]]

      ]]
   [:div.modal-footer
    [card-button showme nameclass phoneclass emailclass birthclass adateclass countyclass ]]])))

     

(defn review-info [showme]
  (let [legalname (subscribe [:yourname])
        email (subscribe [:your-email])
        address (subscribe [:your-address])
        phone (subscribe [:your-phone])
        birthdate (subscribe [:birthdate])
        arrest-county (subscribe [:arrest-county])
        arrest-date (subscribe [:arrest-date])]
    (fn []
    [:div
     [:div.container
     [:form.form-horizontal

      [:div.form-group
       [:div.col-xs-6
        [:div#legalName @legalname]]
       [:label.col-xs-6 {:for "legalName"} [:p [:i "Legal name "]]]]

      [:div.form-group
       [:div.col-xs-6
        [:div#birthdate @birthdate]]
       [:label.col-xs-6 {:for "birthdate"} [:p [:i "Your date of birth "]]]]

      [:div.col-xs-6
       [:div#arrestcounty @arrest-county]]
      [:div.form-group
       [:label.col-xs-6 {:for "arrestcounty"} [:p [:i "Your county of arrest "]]]]

      [:div.col-xs-6
       [:div#arrestdate @arrest-date]]
      [:div.form-group
       [:label.col-xs-6 {:for "arrestdate"} [:p [:i "Your date of arrest "]]]]

      [:div.col-xs-6
       [:div#email @email]]
      [:div.form-group
       [:label.col-xs-6 {:for "email"} [:p [:i "Your email "]]]]

      [:div.col-xs-6
       [:div#address @address]]
      [:div.form-group
       [:label.col-xs-6 {:for "address"} [:p [:i "Your address "]]]]

      [:div.col-xs-6
       [:div#number @phone]]
      [:div.form-group
       [:label.col-xs-6 {:for "number"} [:p [:i "Your phone number "]]]]]]

     [:br]
     [:div.container
      [:div.row.col-md-4.col-centered
      [:div {:class "col-xs-6"}
       [blue-swap-button showme "Edit"]]
      [:div {:class "col-xs-6"}
       [change-page-button "/checkout"]]]]])))


(defn data-review []
  (let [showme (atom true)]
    (fn []
     [:div.form-centered
      [:h6 "Please give us as much information as you can."]

      [:div
       (if @showme
         [review-info showme]
         [edit-info showme])]])))


(defn data-review-page []
  (let [showme (atom true)]
    (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]  
      [data-review]
     [:br]

     [footer]]

     ])))


(secretary/defroute "/review" []
  (session/put! :current-page #'data-review-page))
