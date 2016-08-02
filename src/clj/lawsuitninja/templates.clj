(ns lawsuitninja.templates
  (:require [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [clojure.java.io :as io]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [net.cgrand.enlive-html :refer [deftemplate 
                                            set-attr 
                                            html-resource
                                            defsnippet
                                            at
                                            emit*]])
  (import java.io.StringReader))



(defn home-page [token]
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:meta {:name "Description" :content "Expunge your record without a lawyer for half the price. Free online interview, let's see if we can clear your criminal record, refer your friends for rewards."}]
     [:meta {:name "geo.placename" :content "Texas, United States"}]
     [:meta {:name "fragment" :content "!"}]
     [:meta {:name "keywords" :content "expungement, background check, clear your record, seal your record, misdemeanor, DWI, DUI, felony record, federal record, Class A, Class B, Class C, deferred adjudication, case dismissed, probation, community supervision, expungement cost, failed credit check, criminal record, failed background check"}]
     [:title "Expunge Your Record Without a Lawyer: Expunct"]
     (include-css "css/bootstrap.min.css")
     (include-css "css/flat-ui-pro.css")
     (include-js "/js/googanalyticscode.js")]

    [:body
     [:div#app]
     [:div {:id "anti-forgery-token" :value token}]
     (include-js
    ;  "/js/liveengage.js"

      "/js/flat-ui-pro.min.js"
     "https://code.jquery.com/jquery-2.1.1.min.js"
     "https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js"

     "//userlike-cdn-widgets.s3-eu-west-1.amazonaws.com/43b5edfe04fe3bbd1231b065f2a6ef64a694c69976b4b5b8bda12169918b5600.js"
      "/js/zxcvbn.async.js"

      "/js/app.js")]]))



(def error-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "css/vendor/bootstrap.min.css")
     (include-css "css/flat-ui-pro.css")]

    [:body.error-backgrounder
     [:div.leftcol 
      [:h3 "Sorry, that page can't be found!"]
      [:h5 "Drop us a note and we'll add some helpful resources soon!"]]]]))
