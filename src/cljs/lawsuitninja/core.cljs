(ns lawsuitninja.core
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require clsjs.bootstrap)
  (:require 
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljsjs.react :as react]
            [lawsuitninja.homepage :refer [home-page]]
            [lawsuitninja.payup :refer [payup]]
            [lawsuitninja.firstform :refer [firstform]]
            [lawsuitninja.contact :refer [contact-info]]
            [lawsuitninja.about]
            [lawsuitninja.calculator :refer [calculator-page]]
            [lawsuitninja.login :refer [login-page]]
            [lawsuitninja.register-page :refer [register-page]]
            [lawsuitninja.reset-password :refer [reset-password]]
            [lawsuitninja.personal-profile :refer [profile-page]]
            [lawsuitninja.sub]
            [lawsuitninja.expungeable]
            [lawsuitninja.probation]
            [lawsuitninja.nocharge]
            [lawsuitninja.identitytheft]
            [lawsuitninja.recordrecover]
            [lawsuitninja.expunctioninterview]
            [lawsuitninja.acquittal]
            [lawsuitninja.classCmisdemeanor]
            [lawsuitninja.ABFelony]
            [lawsuitninja.nocontestguilty]
            [lawsuitninja.sealed]
            [lawsuitninja.dismissed]
            [lawsuitninja.notsure]
            [lawsuitninja.checkout]
            [lawsuitninja.nonexpungeable]
            [lawsuitninja.datareview]            
            [lawsuitninja.glossary]
            [lawsuitninja.court-map :refer [plain-map]]
            [lawsuitninja.components :refer [tool-tipper]]
            [lawsuitninja.db :refer [default-value]]
           [lawsuitninja.handlers]) ;; This namespace must always be required. Else Google Closure will drop it and the app won't run.
  ;; (:import goog.history.Html5History
  ;;          goog.Uri))

  (:import goog.History
           goog.Uri))


;; -------------------------
;; Views

(declare top-panel)

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'top-panel))

(secretary/defroute "/firstform" []
  (session/put! :current-page #'firstform))

(secretary/defroute "/payup" []
  (session/put! :current-page #'payup))

(secretary/defroute "/contact-info" []
  (session/put! :current-page #'contact-info))

;; ------------------------
;; Google Analytics
(defn ga 
  "Wrap Google Analytics.
  Source: <https://coderwall.com/p/s3j4va/google-analytics-tracking-in-clojurescript>"
  [& more]
  (when js/ga
    (.. (aget js/window "ga")
        (apply nil (clj->js more)))))

(defn send-page-view
  "Sets `page` as the current page and sends a hit."
  [page]
  (ga "set" "page" page)
  (ga "send" "pageview"))

;; -------------------------
;; History
;; must be called after routes have been defined



(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (send-page-view (.-token event))
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))



(register-handler                 ;; setup initial state
  :initialise-db                     ;; usage:  (submit [:initialize-db])
  (fn
    [db _]
    (merge db default-value)))

(register-sub       ;; we can check if there is data
  :initialised?     ;; usage (subscribe [:initialised?])
  (fn [db]
    (reaction (seq @db))))   ;; do we have data

(defn spinner []
  [:div
   [:div.spinner 
    [:div.double-bounce1]
    [:div.double-bounce2]]
   [:p.form-centered "Initialising app....one moment please"]])   ;; tell them we are working on it


(defn top-panel
  "Checks to see if the data is ready. If not, returns a waiting page.
  If it is, loads the home-page."
  []
  (let [ready?  (subscribe [:initialised?])]
    (fn []
      (if-not @ready?         ;; do we have good data?
        [spinner]
        [home-page]))))      ;; all good, render this component


;; -------------------------
;; Initialize app


(defn ^:export mount-root     ;; call this to bootstrap your app
  []
  (dispatch [:initialise-db])
  (reagent/render [current-page]
                  (js/document.getElementById "app")))

(defn init! []
;  (accountant/configure-navigation!)
  (hook-browser-navigation!)
  (mount-root))
