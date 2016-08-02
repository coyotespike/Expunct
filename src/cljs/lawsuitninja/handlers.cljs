(ns lawsuitninja.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe
                                   debug
                                   after]]
            [schema.core :as s    
             :include-macros true]
            [lawsuitninja.db :refer [valid-schema?]]
  [clairvoyant.core :as trace :include-macros true]))


(defn log-ex
  [handler]
  (fn log-ex-handler
    [db v]
    (try
        (handler db v)        ;; call the handler with a wrapping try
        (catch :default e     ;; ooops
          (do
            (.error js/console e.stack)   ;; print a sane stacktrace
            (throw e))))))


(def standard-middlewares (if ^boolean goog.DEBUG
                            (comp log-ex debug (after valid-schema?))))



(defn set-typeahead-hidden
  [db [_ bool]]
  (assoc-in db [:typeahead :hidden?] bool))

(register-handler
 :set-typeahead-hidden
 standard-middlewares
 set-typeahead-hidden)

(defn set-typeahead-index
  [db [_ ind]]
  (assoc-in db [:typeahead :index] ind))

(register-handler
 :set-typeahead-index
 standard-middlewares
 set-typeahead-index)

(defn select-typeahead
  [db [_ selections]]
  (assoc-in db [:typeahead :selections] selections))

(register-handler
 :select-typeahead
 standard-middlewares
 select-typeahead)

(defn set-typeahead-val
  [db [_ val]]
  (assoc-in db [:typeahead :value] val))

(register-handler
 :set-typeahead-val 
 standard-middlewares
 set-typeahead-val)

(defn set-mouse-on-list
  [db [_ bool]]
  (assoc-in db [:typeahead :mouse-on-list?] bool))

(register-handler
 :set-mouse-on-list 
 standard-middlewares
 set-mouse-on-list)

(register-handler
 :enter-related-charges
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :related-charges name)))

(register-handler
 :enter-state
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :state name)))

(register-handler
 :enter-background-check-purchase
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :background-check-purchase name)))

(register-handler
 :enter-expunction-purchase
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :expunction-purchase name)))


(register-handler
 :enter-case-disposition
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :case-disposition name)))

(register-handler
 :enter-dismissal-date
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :dismissal-date name)))


(register-handler
 :enter-arrest-county
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :arrest-county name)))

(register-handler
 :enter-arrest-date
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :arrest-date name)))

(register-handler
 :enter-birthdate
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :birthdate name)))


(register-handler
 :enter-first-name
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :first-name name)))

(register-handler
 :enter-last-name
 standard-middlewares
 (fn [db [_ name]]
   (assoc db :last-name name)))

(register-handler
   :change-logged-in?
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :logged-in? name)))

(register-handler
   :enter-your-email
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :your-email name)))

(register-handler
   :enter-contact
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :contact name)))

(register-handler
   :enter-yourname
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :yourname name)))

(register-handler
   :enter-your-signature
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :your-signature name)))

(register-handler
   :enter-your-printed-name
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :your-printed-name name)))

(register-handler
   :enter-your-address
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :your-address name)))

(register-handler
   :enter-city-state
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :city-state name)))

(register-handler
   :enter-your-phone
   standard-middlewares
   (fn [db [_ name]]
     (assoc db :your-phone name)))


(register-handler
 :revamp-db
 standard-middlewares
 (fn [db something]
   (merge db something)))



(register-handler            ;; when the GET succeeds 
  :process-service-1-response    
  standard-middlewares
  (fn
    [db [_ response]]
    (merge db response)))
