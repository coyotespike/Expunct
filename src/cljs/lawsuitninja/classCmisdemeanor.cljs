(ns lawsuitninja.classCmisdemeanor
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.db :refer [update-us]]
   [reagent-modals.modals :as reagent-modals]
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
                                    popover
                                    days-between]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]])
  (:import goog.History)
  (:require-macros [reagent.ratom :refer [reaction]]))

(defmulti change-page (fn [model] model))
(defmethod change-page "relations" []
  (set! (.-hash js/window.location) "/relatedcharges"))
(defmethod change-page "norelation" []
  (set! (.-hash js/window.location) "/norelations"))


(defn related-charges []
  [:div
   [:h6 "Don't worry, we'll keep this short."]
   [:p "Charges are related if you got them at the same time, for the same episode."]
   [:p "For instance, if you were charged with a DUI, and also with driving with an open container, those charges are related, right?"]
   [:p "But if you had a DUI, and a different time got charged for shoplifting or whatever, those charges are not related."]

   [:br]
   [:p "So, you got just a Class C offense."]
   [:p "Were there any other related charges?"]])

(def norelation "I only had one charge, or any other charges had nothing to do with this one.")
(def relations "Yes, I had multiple charges that came out of the same event.")


(defn classC []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
    (fn []
      [:div.Site
       [reagent-modals/modal-window]
       [:div.wrapper
        [:div.page-header
         [navbar]]
        [:div.form-centered
         [:h5 "Okay, one more question."]
         [:p "If you were convicted of a Class C misdemeanor, we need to know one more thing."]

         [:br]

         [:h6 "Do you have any related charges? "
          [:span {:class "fui-question-circle" 
                  :data-toggle "tooltip" 
                  :title "Click the question mark to find out what this is."
                  :on-click #(reagent-modals/modal! [related-charges])
                  :data-placement "right"}]]]

        [:div.container
         [:div.row
          [:div.col-xs-4.col-sm-4.col-md-6.col-lg-6
           [v-box
            :width "300px"
            :children [[:p @description]]]]
          [:div.col-xs-8.col-sm-8.col-md-3.col-lg-3.col-lg-offset-3
           [v-box
            :children [[radio-button 
                        :label "No related charges"
                        :value "norelation"
                        :model model
                        :on-change #(do
                                      (reset! model "norelation")
                                      (dispatch [:enter-related-charges "no related charges"])
                                      (reset! description norelation))]
                       [radio-button 
                        :label "Yes, there were other related charges"
                        :value "relations"
                        :model model
                        :on-change #(do
                                      (reset! model "relations")
                                      (dispatch [:enter-related-charges "other related charges"])
                                      (reset! description relations))]]]]]]
        [:br]
        [:div.form-centered
         [:input {:class "btn btn-info palette-turquoise"
                  :type "button"
                  :value "This one"
                  :on-click #(change-page @model)}]]
        [:br]
        [:div.push]]

       [footer]

       ])))
  
(secretary/defroute "/classC" []
  (session/put! :current-page #'classC))
