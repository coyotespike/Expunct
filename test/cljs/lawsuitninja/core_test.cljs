(ns lawsuitninja.core-test
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [cemerick.cljs.test :refer-macros [is are deftest testing use-fixtures done]]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [register-handler
                                   path
                                   register-sub
                                   dispatch
                                   dispatch-sync
                                   subscribe
                                   debug
                                   after]]
;            [lawsuitninja.handlers :as handlers]
            [lawsuitninja.core :as rc]))


(def isClient (not (nil? (try (.-document js/window)
                              (catch js/Object e nil)))))

(def rflush reagent/flush)

(defn add-test-div [name]
  (let [doc     js/document
        body    (.-body js/document)
        div     (.createElement doc "div")]
    (.appendChild body div)
    div))

(defn with-mounted-component [comp f]
  (when isClient
    (let [div (add-test-div "_testreagent")]
      (let [comp (reagent/render-component comp div #(f comp div))]
        (reagent/unmount-component-at-node div)
        (reagent/flush)
        (.removeChild (.-body js/document) div)))))


(defn found-in [re div]
  (let [res (.-innerHTML div)]
    (if (re-find re res)
      true
      (do (println "Not found: " res)
          false))))


;; (deftest test-home
;;   (with-mounted-component (rc/home-page)
;;     (fn [c div]
;;       (is (found-in #"Welcome to" div)))))

(deftest enter-date-moved
  (is (= "2015-07-19T05:00:00.000-00:00"
         (:enter-date-moved-out 
          {:date-moved-out (js/Date.)} 
          "2015-07-19T05:00:00.000-00:00"))))

(deftest changed-logged-in?
  (is (= true
         (:change-logged-in? {:logged-in? false} true))))
