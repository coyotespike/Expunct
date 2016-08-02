(ns clj.lawsuitninja.test.redistest
  (:use clojure.test)
  (:use [lawsuitninja.redis]))


;; (deftest Redis-checks
;;  "This checks every function currently in redis.clj"
;;   (testing "Testing putting things into Redis and getting them out."
;;     (is (= true (not (nil? (read-from-Redis "roy.tim@gmail.com"))))))

;;   (testing "Updating Redis will return a new value."
;;     (is (= {:hello "hello"} 
;;            (do
;;              (update-Redis {:body 
;;                             {:username "roy.tim@gmail.com"
;;                              :BigAtom {:hello "hello"}}})
;;              (:BigAtom (read-from-Redis "roy.tim@gmail.com"))))))

;;   (testing "Giving Redis a bad password produces an error"
;;     (is (= 403
;;            (:status 
;;             (login-user {:body 
;;                          {:username "roy.tim@gmail.com"
;;                           :password "Secrets"
;;                           :BigAtom {:hello "hello"}}})))))

;;   (testing "Registering creates a new account."
;;     (is (= 200
;;            (:status
;;             (register-user {:body 
;;                             {:username "boyhowdy@gmail.com"
;;                              :password "Secrets"
;;                              :BigAtom {:hello "hello"}}})))))

;;   (testing "Deleting the user, deletes the user."
;;     (is (= 200
;;            (:status
;;             (delete-user "boyhowdy@gmail.com" "Secrets")))

;;         (= nil
;;            (read-from-Redis "boyhowdy@gmail.com")))))
