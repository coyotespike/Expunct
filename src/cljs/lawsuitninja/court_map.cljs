(ns lawsuitninja.court-map
  (:require
   [goog.events :as events]
   [goog.history.EventType :as EventType]
   [goog.dom :as dom]
   [goog.object :as gobj]
   [goog.string :as gstring]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [secretary.core :as secretary :include-macros true]
   [cljs-http.client :as http]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [lawsuitninja.components :refer [navbar
                                    medium-text-box
                                    tool-tipper
                                    popover
                                    save-button
                                    password-switch-buttons
                                    wrap-as-element-in-form
                                    password-form
                                    delete-button
                                    second-password-form]]
   [cljs.core.async :as async :refer [put! <! >! chan]])
  (:require-macros [reagent.ratom :refer [reaction]]
                   [cljs.core.async.macros :refer [go]]))



(defn get-position []
  (let [out (chan)
        geo (.-geolocation js/navigator)]
    (.getCurrentPosition geo (fn [pos] (put! out pos)))
    out))

(defn map-creator []
  (go 
    (let [coords    (.-coords (<! (get-position)))
          lat  (.-latitude coords)
          lon (.-longitude coords)
          center (google.maps.LatLng. lat, lon)
          map-opts (clj->js {:center center
                             :zoom 12})
          map-canvas  (.getElementById js/document "map-canvas")
          mappy    (js/google.maps.Map. map-canvas map-opts)
          request (clj->js {:location center :radius 15000 :types ["courthouse" "court"]})
          service (google.maps.places.PlacesService. mappy)
          marker (google.maps.Marker. (clj->js {:position center
                                                :title "You're somewhere around here"}))
          bounds (google.maps.LatLngBounds.)
          info-window (google.maps.InfoWindow.
                       (clj->js {:content "You're somewhere around here."}))]
      (.setMap marker mappy)
      (google.maps.event.addListener marker "mouseover" 
                                     (fn [] (.open
                                             info-window mappy marker)))

      (.nearbySearch service request #(let [result (clj->js %)]
                                        (loop [[first & rest] result]
                                          (if (some? first)
                                            (do
                                              (let [lat (goog.object.getValueByKeys first "geometry" "location" "G")
                                                    lon (goog.object.getValueByKeys first "geometry" "location" "K")
                                                    vicinity (.-vicinity first)
                                                    name (.-name first)
                                                    marco (google.maps.Marker. 
                                                                    (clj->js
                                                                     {:position 
                                                                      (google.maps.LatLng.
                                                                       lat lon)
                                                                      :title name
                                                                      :animation google.maps.Animation.DROP}))
                                                    info-window (google.maps.InfoWindow.
                                                                 (clj->js {:content 
                                                                           (str "<div> <h6>" name "</h6>"
                                                                                "<p>" vicinity "</p> </div>")}))]
                                               (.setMap marco mappy)
                                               (.extend bounds (.getPosition marco))
;                                               (js/console.log (pr-str (js->clj first)))
                                                (google.maps.event.addListener marco "click" 
                                                                               (fn [] (.open
                                                                                       info-window mappy marco))))
                                              (recur rest))
                                            (do
                                              (.fitBounds mappy bounds)
                                              (js/console.log "All done!")))))))))


(defn map-div []
  [:div
;   [:input {:id "pac-input" :class "controls" :type "text" :placeholder "Search Box"}]
   [:div#map-canvas]])

(defn ^:export load-when-ready []
    (-> (js/$ js/document)
        (.ready
             (fn [e] (map-creator)))))

(defn google-map []
  (reagent/create-class {:reagent-render map-div
                         :component-will-mount load-when-ready}))

(defn plain-map []
  [:div
   [:div.page-header
    [navbar]]
   [google-map]

   [:div.form-centered
    [:h6 "Here are some courts located near you."]
    [:p "(You can find this map on your personal profile page as well.)"]
    [:p "Not sure which court you should file with? No worries."]
    [:p "If you decide to file yourself, we'll tell you which is the right court."]
    [:p "If you decide to have us file for you, then we'll find the right court."]]])

(secretary/defroute "/nearby-courthouses" []
  (session/put! :current-page #'plain-map))
