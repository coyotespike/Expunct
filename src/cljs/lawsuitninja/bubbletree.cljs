(ns lawsuitninja.bubbletree
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljsjs.react :as react]
              [cljs.reader :as reader]

              [lawsuitninja.depositwitheld :refer [left-property]]
              [lawsuitninja.components :refer [atom-input
                                               small-text-box
                                               wide-text-box
                                               medium-text-box
                                               disabled-button
                                               text-area-input
                                               navbar
                                               paginator
                                               tool-tipper]]
              [re-frame.core :refer [register-handler
                                     path
                                     register-sub
                                     dispatch
                                     dispatch-sync
                                     subscribe]])

    (:import goog.History)
    (:require-macros
     [reagent.ratom :refer [reaction]]))


;;; An experiment in adapting a beautiful data visualization.

(def data {
	:label "Which sounds like your situation?",
	:amount 10000
	:children [
                   {:label "<a href='#'> I am the 1st child</a>"
                    :amount 20000
                    :children [
                               {:label "I am the 2nd child",
                                :amount 30000}
                   {:label "I am the 3rd child",
                    :amount 40000}]

                    }, 
                   {:label "I am the 2nd child",
                    :amount 30000}
                   {:label "I am the 3rd child",
                    :amount 40000}
                   {:label "I am the 4th child",
                    :amount 50000}
                   {:label "I am the 4th child",
                    :amount 60000}

]
})

(defn bubble-div []
  [:div {:class "bubbletree-wrapper"}
   [:div {:id "whatever" :class "bubbletree"}]])


(defn home-did-mount []
  (js/$ (fn []
          (js/BubbleTree. 
           (clj->js {:data data
                     :container "#whatever"})))))

(defn bubble []
  (reagent/create-class {:reagent-render bubble-div
                         :component-did-mount home-did-mount}))

(defn bubbler []
  [:div
   [:div.page-header
    [navbar]]
   [bubble]])

(secretary/defroute "/bubbler" []
  (session/put! :current-page #'bubbler))
