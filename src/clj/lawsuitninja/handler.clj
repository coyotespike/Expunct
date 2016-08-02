(ns lawsuitninja.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.transit :refer [wrap-transit-body]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            
            [lawsuitninja.redis :refer [update-Redis
                                        login-user
                                        register-user
                                        delete-user
                                        change-password
                                        random-password]]
            [lawsuitninja.formtemplate :refer [make-pdf]]
            [lawsuitninja.templates :refer [error-page home-page]]
            [lawsuitninja.slacker :refer [join-slack]]
            [lawsuitninja.payments :refer [create-customer]]
            [lawsuitninja.sitemap :refer [sitemap]]
            [lawsuitninja.emails :refer [send-email
                                         pay-email
                                         help-email]]))


;;;; Routes
(defroutes routes
  (GET "/" [] (home-page *anti-forgery-token*))
  (GET "/sitemap.xml" [] (sitemap))
  (POST "/email" {body :body} (send-email body))
  (POST "/checkout" req (create-customer req))
  (POST "/payme" {body :body} (pay-email body))
  (POST "/logmein" req (login-user req))
  (POST "/registerme" req (register-user req))
  (POST "/savemywork" req (update-Redis req))
  (POST "/updatepassword" req (change-password req))
  (POST "/emailrandompassword" req (random-password req))
  (POST "/deleteprofile" req (delete-user req))
  (resources "/")
  (not-found error-page))


(def app
  (let [handler 
        (wrap-transit-body 
         (wrap-defaults routes site-defaults))]
    (if (env :dev) 
      (wrap-reload (wrap-exceptions handler))
      handler)))
