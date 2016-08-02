(ns lawsuitninja.components-testing
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
            [lawsuitninja.components :as components]))

;; (deftest calculator-test
;;   (is (= 
