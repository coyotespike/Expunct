(ns lawsuitninja.db-test
  (:require [cemerick.cljs.test :refer-macros [is are deftest testing 
                                               use-fixtures done]]
            [lawsuitninja.db :as db]))

;; (deftest token-check
;;   (testing "We can accurately get the token"
;;     (is (> 20 (len (db/token))))))
