(ns lawsuitninja.acquittal
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
                                    days-between
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



(defmulti change-page (fn [model] model))
(defmethod change-page "relations" []
  (set! (.-hash js/window.location) "/relatedcharges"))
(defmethod change-page "norelation" []
  (set! (.-hash js/window.location) "/norelations"))
(defmethod change-page "allclear" []
  (set! (.-hash js/window.location) "/cleared"))
(defmethod change-page "pending" []
  (set! (.-hash js/window.location) "/not-cleared"))
(defmethod change-page "classC" []
  (set! (.-hash js/window.location) "/classC"))


(defn not-cleared []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h5 "We're very sorry"]
      [:p "If you were convicted of a related crime, or still have related charges pending, you can't have this charge expunged."]
      [:p "If you're not completely sure, you should still have us pull your record and look at it."]
      [public-purchase-button showme]]
     [:br]
     [:div.push]]

     [footer]
])))

(defn norelations []
  (insert-stripe)
  (let [showme (atom "a")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h6 "Excellent."]
      [:p "If there were no other related charges, then you can get an expunction."]
      [:p "Let's pull your file and get to work!"]
      [public-purchase-button showme]]
     [:br]
     [:div.push]]

     [footer]
])))

(defn cleared []
  (insert-stripe)
  (let [showme (atom "a")]
    (fn []
      [:div.Site
       [:div.wrapper
        [:div.page-header
         [navbar]]
        [:div.form-centered
         [:h6 "Congratulations!"]
         [:p "Because you were completely acquitted, or any other charges were minor, we can clear your record."]
         [:p "Let's pull your file and get to work!"]
         [public-purchase-button showme]]
        [:br]
        [:div.push]]

       [footer]

       ])))


(def allclear "I was acquitted of all charges, or the other charges are Class C.")
(def pending "I was convicted of a related crime, or related crimes are still pending, and they're not Class C.")

(defn other-relations []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
  (fn []
    [:div.Site
     [:div.wrapper
     [:div.page-header
      [navbar]]
     [:div.form-centered
      [:h6 "Okay, last question, for real this time."]
      [:p "Were you acquitted of those charges, or were they class C misdemeanors?"]]

     [:div.container
      [:div.row
       [:div.col-xs-4.col-sm-4.col-md-6.col-lg-6
        [v-box
;         :width "300px"
         :children [[:p @description]]]]
       [:div.col-xs-8.col-sm-8.col-md-6.col-lg-3.col-lg-offset-3
        [v-box
         :children [[radio-button 
                     :label "Yes, I was acquitted or they were minor offenses."
                     :value "allclear"
                     :model model
                     :on-change #(do
                                   (reset! model "allclear")
                                     (dispatch [:enter-related-charges "related offenses were minor or acquitted"])
                                   (reset! description allclear))]
                    [radio-button 
                     :label "No, there are other serious convictions or charges."
                     :value "pending"
                     :model model
                     :on-change #(do
                                   (reset! model "pending")
                                     (dispatch [:enter-related-charges "other serious charges"])
                                   (reset! description pending))]]]]]]
     [:br]
     [:div.form-centered
      [:input {:class "btn btn-info palette-turquoise"
               :type "button"
               :value "This one"
               :on-click #(change-page @model)}]]
     [:br]
      ]

     [footer]

])))


(defn related-charges []
  [:div
   [:h6 "Don't worry, we'll keep this short."]
   [:p "Charges are related if you got them at the same time, for the same episode."]
   [:p "For instance, if you were charged with a DUI, and also with driving with an open container, those charges are related, right?"]
   [:p "But if you had a DUI, and a different time got charged for shoplifting or whatever, those charges are not related."]

   [:br]
   [:p "So, you got acquitted of a charge (congratulations!)."]
   [:p "Were there any related charges?"]])

(def norelation "I only had one charge, or any other charges had nothing to do with this one.")
(def relations "Yes, I had multiple charges that came out of the same event.")

(defn acquittal []
  (let [model (atom "green")
        description (atom "Choose one for more information")]
       [reagent-modals/modal-window]
    (fn []
      [:div.Site
       [:div.wrapper
        [:div.page-header
         [navbar]]
        [:div.form-centered
         [:h5 "Okay, one more question."]
         [:p "If you went to trial and were acquitted, we need to know one more thing."]

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

(secretary/defroute "/acquittal" []
  (session/put! :current-page #'acquittal))

(secretary/defroute "/norelations" []
  (session/put! :current-page #'norelations))

(secretary/defroute "/relatedcharges" []
  (session/put! :current-page #'other-relations))

(secretary/defroute "/cleared" []
  (session/put! :current-page #'cleared))

(secretary/defroute "/not-cleared" []
  (session/put! :current-page #'not-cleared))

