(ns lawsuitninja.personal-profile
  (:require
   [goog.events :as events]
   [goog.history.EventType :as EventType]
   [goog.dom :as dom]
   [goog.object :as gobj]
   [goog.string :as gstring]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :include-macros true]
   [cljs-http.client :as http]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [lawsuitninja.db :refer [update-us]]
   [lawsuitninja.components :refer [navbar
                                    footer
                                    interview-button
                                    alt-picker
                                    blue-swap-button
                                    medium-text-box
                                    tool-tipper
                                    popover
                                    change-page-button
                                    save-button
                                    password-switch-buttons
                                    wrap-as-element-in-form
                                    password-form
                                    delete-button
                                    second-password-form
                                    public-purchase-button
                                    insert-stripe]]
   [cljs.core.async :as async :refer [put! <! >! chan]])
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]]))





(defn review-save-button [showme] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Save"
           :on-click #(do
                        (.-preventDefault (swap! showme not))
                        (update-us))}]))


(defn edit-info [showme]
  [:div
   [:div.container
    [:div.row.row-centered

     [:div.col-md-6.col-centered
      [:form.form-horizontal {:role "form"}

       [:div.form-group
        [:div.col-sm-6 [medium-text-box :yourname :enter-yourname]]
        [:div.col-sm-6 [:i "Legal name "]]]

       [:div.form-group
        [:div.col-sm-6 [alt-picker :birthdate]]
        [:div.col-sm-6 [:i "Your date of birth "]]]

       [:div.form-group
        [:div.col-sm-6 [medium-text-box :your-email :enter-your-email]]
        [:div.col-sm-6 [:i "Your email "]]]

       [:div.form-group
        [:div.col-sm-6
         [medium-text-box :arrest-county :enter-arrest-county]]
        [:div.col-sm-6 [:i "County of arrest"]]]

       [:div.form-group
        [:div.col-sm-6 [alt-picker :arrest-date]]
        [:div.col-sm-6 [:i "Date of arrest"]]]

       [:div.form-group
        [:div.col-sm-6 [medium-text-box :your-address :enter-your-address]]
        [:div.col-sm-6 [:i "Your address "]]]

       [:div.form-group
        [:div.col-sm-6 [medium-text-box :your-phone :enter-your-phone]]
        [:div.col-sm-6 [:i "Your phone number "]]]
       ]]]]

   [review-save-button showme]])

(defn review-info [showme]
  (insert-stripe)
  (let [legalname (subscribe [:yourname])
        email (subscribe [:your-email])
        address (subscribe [:your-address])
        phone (subscribe [:your-phone])
        birthdate (subscribe [:birthdate])
        arrest-county (subscribe [:arrest-county])
        arrest-date (subscribe [:arrest-date])]
    (fn []
      [:div
       [:div.parent-container
        [:div.child-column
         [:p [:i "Legal name "]]
         [:p [:i "Your date of birth "]]
         [:p [:i "County of arrest"]]
         [:p [:i "Date of arrest"]]
         [:p [:i "Your email "]]
         [:p [:i "Your address "]]
         [:p [:i "Your phone number "]]]
        [:div.child-column
         [:p @legalname]
         [:p @birthdate]
         [:p @arrest-county]
         [:p @arrest-date]
         [:p @email]
         [:p @address]
         [:p @phone]]]
       [:br]
       [:div.container
        [:div.row.col-md-4.col-centered
         [:div {:class "col-xs-6"}
          [blue-swap-button showme "Edit"]]
         [:div {:class "col-xs-6"}]]]])))



(defn profile-page []
  (let [background-check-purchase (subscribe [:background-check-purchase])
        expunction-purchase (subscribe [:expunction-purchase])
        legalname (subscribe [:yourname])
        email (subscribe [:your-email])
        address (subscribe [:your-address])
        phone (subscribe [:your-phone])
        password (atom "New password")
	second-password (atom "confirm password")
        tester (atom false)
        showme (atom true)
        showpurchase (atom "a")]
    (fn []
      [:div.Site
       [:div.wrapper
        [:div.page-header
         [navbar]]
        [:div.centered-tabs
         [:ul {:class "nav nav-tabs" :role "tablist"}
          [:li {:role "presentation" :class "active"} [:a {:href "#profile" :aria-controls "profile" :role "tab" :data-toggle "tab"} "Your Profile"]]
          [:li {:role "presentation"} [:a {:href "#purchases" :aria-controls "purchases" :role "tab" :data-toggle "tab"} "Your Purchases"]]
          [:li {:role "presentation"} [:a {:href "#password" :aria-controls "password" :role "tab" :data-toggle "tab"} "Change Your Password"]]
          ]
         [:div {:class "tab-content"}

          [:div {:role "tabpanel" :class "tab-pane active fade in" :id "profile"}
           [:div.form-centered
            (if @showme
              [review-info showme]
              [edit-info showme])]]

          [:div {:role "tabpanel" :class "tab-pane fade" :id "purchases"} 

           [:div.container
            [:div.row
             [:div.col-xs-6.col-sm-6.col-md-6.col-lg-6
              [:p (if (= "Not yet!" @background-check-purchase)
                    [public-purchase-button showpurchase]
                    @background-check-purchase)]]
             [:div.col-xs-6.col-sm-6.col-md-6.col-lg-6
              [:p "Background check and file pull: " ]]]

            [:div.row
             [:div.col-xs-6.col-sm-6.col-md-6.col-lg-6
              [:p @expunction-purchase]]
             [:div.col-xs-6.col-sm-6.col-md-6.col-lg-6
              [:p "Expunction or nondisclosure: " ]]]]]
          [:div {:role "tabpanel" :class "tab-pane fade" :id "password"} 
           [wrap-as-element-in-form [password-form password]]
           [wrap-as-element-in-form [second-password-form second-password]]
           [password-switch-buttons password second-password]]
                    ]]]

       [footer]
       ])))

(secretary/defroute "/dashboard" []
  (session/put! :current-page #'profile-page))
