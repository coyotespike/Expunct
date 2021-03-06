(ns lawsuitninja.pikaday
    (:require [reagent.core :as reagent :refer [atom]]
              [re-frame.core :refer [register-handler
                                     path
                                     register-sub
                                     dispatch
                                     dispatch-sync
                                     subscribe]]
              [camel-snake-kebab.core :refer [->camelCaseString]]
              [camel-snake-kebab.extras :refer [transform-keys]]
              [cljsjs.pikaday]
              [clairvoyant.core :as trace :include-macros true])
  (:require-macros [reagent.ratom :refer [reaction]]))

; cf https://github.com/thomasboyt/react-pikaday/blob/master/src/Pikaday.js

(defn- opts-transform [opts]
  "Given a clojure map of options, return a js object for a pikaday constructor argument."
  (clj->js (transform-keys ->camelCaseString opts)))

(defn- watch [ratom predicate func])


(defmulti date-dispatcher (fn [date] (:date-type date)))

(defmethod date-dispatcher :forwarding-address [date]
  (let [date (:date date)
        nice-date (.toLocaleDateString date "en" "%d-%b-%Y")
        the-facts (subscribe [:the-facts])]
     (dispatch [:enter-date-moved-out date])
     (dispatch [:enter-the-facts (str @the-facts 
                               " Plaintiff provided Defendant a forwarding address on "
                               nice-date
                               ".")])))

(defmethod date-dispatcher :birthdate [date]
  (let [date (:date date)]
     (dispatch [:enter-birthdate date])))

(defmethod date-dispatcher :arrest-date [date]
  (let [date (:date date)]
     (dispatch [:enter-arrest-date date])))

(defmethod date-dispatcher :dismissal-date [date]
  (let [date (:date date)]
     (dispatch [:enter-dismissal-date date])))



(defn date-selector
  "Return a date-selector reagent component. Takes a single map as its 
  argument, with the following keys:
  date-atom: an atom or reaction bound to the date value represented by the picker.
  max-date-atom: atom representing the maximum date for the selector.
  min-date-atom: atom representing the minimum date for the selector.
  pikaday-attrs: a map of options to be passed to the Pikaday constructor.
  input-attrs: a map of options to be used as <input> tag attributes."
  [{:keys [date-atom max-date-atom min-date-atom pikaday-attrs input-attrs date-type]}]
  (let [instance-atom (atom nil)]
    (reagent/create-class
      {:component-did-mount
        (fn [this]
          (let [default-opts
                {:field (.getDOMNode this)
                 :default-date @date-atom
                 :set-default-date true
;;; changed from, from re-frame
;                :on-select #(when date-atom (reset! date-atom %))}
                 :on-select #(when date-atom (date-dispatcher {:date % :date-type date-type}))}
                opts (opts-transform (merge default-opts pikaday-attrs))
                instance (js/Pikaday. opts)]
            (reset! instance-atom instance)
            ; This code could probably be neater
            (when date-atom
              (add-watch date-atom :update-instance
                (fn [key ref old new]
                  ; final parameter here causes pikaday to skip onSelect() callback
                  (.setDate instance new true))))
            (when min-date-atom
              (add-watch min-date-atom :update-min-date
                (fn [key ref old new]
                  (.setMinDate instance new)
                  ; If new max date is less than selected date, reset actual date to max
                  (if (< @date-atom new)
                    (reset! date-atom new)))))
            (when max-date-atom
              (add-watch max-date-atom :update-max-date
                (fn [key ref old new]
                  (.setMaxDate instance new)
                  ; If new max date is less than selected date, reset actual date to max
                  (if (> @date-atom new)
                    (reset! date-atom new)))))))
       :component-will-unmount
       (fn [this]
         (.destroy @instance-atom)
         (remove-watch instance-atom :update-instance)
         (remove-watch instance-atom :update-min-date)
         (remove-watch instance-atom :update-max-date)
         (reset! instance-atom nil))
       :display-name "pikaday-component"
       :reagent-render
        (fn [input-attrs]
          [:input input-attrs])})))




(defn date-selector2
  "Return a date-selector reagent component. Takes a single map as its 
  argument, with the following keys:
  date-atom: an atom or reaction bound to the date value represented by the picker.
  max-date-atom: atom representing the maximum date for the selector.
  min-date-atom: atom representing the minimum date for the selector.
  pikaday-attrs: a map of options to be passed to the Pikaday constructor.
  input-attrs: a map of options to be used as <input> tag attributes."
  [{:keys [date-atom max-date-atom min-date-atom pikaday-attrs input-attrs]}]
  (let [instance-atom (atom nil)]
    (reagent/create-class
      {:component-did-mount
        (fn [this]
          (let [default-opts
                {:field (.getDOMNode this)
                 :default-date @date-atom
                 :set-default-date true
                :on-select #(when date-atom (reset! date-atom %))}
                opts (opts-transform (merge default-opts pikaday-attrs))
                instance (js/Pikaday. opts)]
            (reset! instance-atom instance)
            ; This code could probably be neater
            (when date-atom
              (add-watch date-atom :update-instance
                (fn [key ref old new]
                  ; final parameter here causes pikaday to skip onSelect() callback
                  (.setDate instance new true))))
            (when min-date-atom
              (add-watch min-date-atom :update-min-date
                (fn [key ref old new]
                  (.setMinDate instance new)
                  ; If new max date is less than selected date, reset actual date to max
                  (if (< @date-atom new)
                    (reset! date-atom new)))))
            (when max-date-atom
              (add-watch max-date-atom :update-max-date
                (fn [key ref old new]
                  (.setMaxDate instance new)
                  ; If new max date is less than selected date, reset actual date to max
                  (if (> @date-atom new)
                    (reset! date-atom new)))))))
       :component-will-unmount
       (fn [this]
         (.destroy @instance-atom)
         (remove-watch instance-atom :update-instance)
         (remove-watch instance-atom :update-min-date)
         (remove-watch instance-atom :update-max-date)
         (reset! instance-atom nil))
       :display-name "pikaday-component"
       :reagent-render
        (fn [input-attrs]
          [:input input-attrs])})))
