(ns lawsuitninja.components
  (:require 
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [clojure.string :as s]
   [dommy.core :refer-macros [sel sel1]]
   [dommy.core :refer [hide! show! remove-attr!]]
   [cljs-http.client :as http]
   [enfocus.core :as ef]
   [goog.dom.classlist :as classlist]
   [secretary.core :as secretary :include-macros true]

   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [reagent-forms.core :refer [bind-fields init-field value-of]]
   [lawsuitninja.pikaday :as pikaday]
   [lawsuitninja.termsandconditions :refer [privacy-modal tc-modal]]
   [lawsuitninja.db :refer [pay-up
                            email-us
                            update-us
                            register-user
                            login-user
                            new-password
                            delete-profile
                            email-random-password
                            lookup-record]]
   [clairvoyant.core :as trace :include-macros true]
   [cljs.core.async :as async :refer [put! <! >! chan timeout]]
   [reagent-modals.modals :as reagent-modals])
  (:require-macros [reagent.ratom :refer [reaction]]
                   [lawsuitninja.mismacros :as mymacros]
                   [reagent-forms.macros :refer [render-element]]
                   [cljs.core.async.macros :refer [go]]))


;;; This page contains dozens of buttons, sortable portlets, datepickers, state
;;; pickers, headers, reactive switching of buttons, polls, card validators,
;;; login status validators, modals, etc. If you can think of it it's probably
;;; here. I began using re-com toward the end.

(defn linky-button [text function]
  (fn []
     [:input {:class "linky-button"
              :type "button"
              :value text
              :on-click function}]))

(defn switch-buttons [atom default alternative]
  (fn []
    (if @atom
      default
      alternative)))

(defn blue-swap-button [showme label]
  (fn []
     [:input {:class "btn btn-info palette-turquoise"
              :type "button"
              :value label
              :on-click #(.-preventDefault (swap! showme not))}]))

(defn interview-button [page] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value "Interview"
           :on-click #(set! (.-hash js/window.location) page)}]))


(defn change-page-button [page] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Keep thinking"
           :on-click #(set! (.-hash js/window.location) page)}]))
                                        ;(secretary/dispatch! page)}]))

;; Without the outer fn, the component refused to render, with error.
;; Invalid Hiccup form, nil.
(defn disabled-button  [displayvalue] 
  [:input {:type "button" 
           :class "btn btn-default.disabled:active" 
           :value displayvalue}])


(defn new-password-button [] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Confirm"
           :on-click #(do
                        (email-random-password)
                        (js/history.back))}]))

(defn wait-button [] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value "Send"
           :on-click #(js/alert "Sorry, we need your email!")}]))

(defn pay-wait [] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value "Take our money"
           :on-click #(js/alert "Did you fill everything out?")}]))

(defn save-button [] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Save"
           :on-click #(update-us)}]))


(defn email-button [showme] 
  (fn []
    [:input {:type "button" 
             :class "btn btn-primary" 
             :value "Shazam"
             :on-click #(do
                          (.-preventDefault (swap! showme not))
                          (email-us :contact-us))}]))

(defn pay-button [showme] 
  (fn []
    [:input {:type "button" 
             :class "btn btn-primary" 
             :value "Shazam"
             :on-click #(do
                          (.-preventDefault (swap! showme not))
                          (pay-up))}]))


 (defn back-button []
   (fn []
     [:input {:type "button"
              :class "btn btn-primary"
              :value "Take me back!" 
              :on-click #(js/history.back)}]))

(defn name-input []
  (let [namer (subscribe [:yourname])]
    (fn []
      [:input {:type "text"
               :placeholder @namer
               :on-change #(dispatch [:enter-yourname 
                                      (-> % .-target .-value)])}])))

(defn name-input2 [sub handler]
  (let [namer (subscribe [sub])]
    (fn []
      [:input {:type "text"
               :placeholder @namer
               :on-change #(dispatch [handler 
                                      (-> % .-target .-value)])}])))


(defn atom-input [sub handler]
  (let [value (subscribe [sub])]
    (fn []
      [:input {:type "text"
               :placeholder @value
               :on-change #(dispatch [handler 
                                      (-> % .-target .-value)])}])))

(defn wide-text-box [sub handler]
  (let [value (subscribe [sub])]
    (fn []      
      [:input {:type "text"
               :placeholder @value
               :size 40
               :on-change #(dispatch [handler (-> % .-target .-value)])}])))


(defn small-text-box [sub handler]
  (let [value (subscribe [sub])]
    (fn []
      [:input {:type "text"
               :placeholder @value
               :size 2
               :on-change #(dispatch [handler (-> % .-target .-value)])}])))

(defn medium-text-box [sub handler]
  (let [value (subscribe [sub])]
    (fn []
      [:input.form-control {:type "text"
                            :placeholder @value
                            :size 20
                            :on-change #(dispatch [handler 
                                                   (-> % .-target .-value)])}])))


(defn text-area-input [sub handler]
  (let [value (subscribe [sub])]
  (fn []
    [:textarea.form-notes {:rows 4 
                           :cols 80
                           :value @value
                           :on-change #(dispatch [handler 
                                                  (-> % .-target .-value)])}])))

(defn text-area-output [sub handler]
  (let [value (subscribe [sub])]
    (fn []
    [:div.form-group.has-feedback
      [:textarea
       {:class "form-control input-hg"
        :rows 6
        :cols 80
        :placeholder @value
        :on-change #(dispatch [handler (-> % .-target .-value)])}]])))

(defn dropdown-element []
     [:ul
      {:class "nav navbar-nav"}
      [:li
       {:class "dropdown"}
       [:a
        {:href "#",
         :class "dropdown-toggle",
         :data-toggle "dropdown",
         :role "button",
         :aria-expanded "false"
         :aria-haspopup "true"}
        "Topics"
        [:span {:class "caret"}]]
       [:ul
        {:class "dropdown-menu", :role "menu"}
        [:li [:a {:href "#"} "Home"]]
        [:li {:class "divider"}]
        [:li [:a {:href "#/firstform"} "Basic Form"]]
        [:li [:a {:href "#/firstinterview"} "The Funnel"]]]]])


(defn user-name []
  (fn []
  [:div {:class "navbar-right"}
   [:a {:href "#/dashboard" :type "button" 
        :class "btn btn-default navbar-btn"} "My Account"]
   [:a {:on-click #(dispatch [:initialise-db]) :href "/" :type "button"
        :class "btn btn-social-linkedin navbar-spacer"} "Log Out"]]))

(defn register-login []
  (fn []
  [:div {:class "nav nav-pills nav-stacked navbar-right"}
   [:a {:href "#/login" :type "button" 
        :class "btn btn-oranj navbar-btn"} "Login"]
]))

;; ---------------- Navbar -----------------
(defn navbar []
  (let [logged-in? (subscribe [:logged-in?])]
  [:nav {:class "navbar navbar-inverse navbar-fixed-top" :role "navigation"}
   [:div {:class "container-fluid"}
    [:div {:class "navbar-header"}
     [:button
      {:type "button",
       :class "navbar-toggle ",
       :data-toggle "collapse",
       :data-target "#bs-example-navbar-collapse-1"
       :aria-expanded "false"}
      [:span {:class "sr-only"} "Toggle navigation"]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]]
     [:a {:class "navbar-brand", :href "#"} "Expunct.com"]

     [:img {:src "/img/614px-Texas_flag_map.svg.png" :height "45px"}]
]
    [:div
     {:class "collapse navbar-collapse",
      :id "bs-example-navbar-collapse-1"}
;     [dropdown-element]
     [:ul
      {:class "nav navbar-nav"}
      [:li [:a {:href "#/contact-info"} "Ask a question"]]
      [:li [:a {:href "#/FAQ"} "FAQs"]]
;      [:li [:a {:href "#/glossary"} "Team Blog"]]
 ;     [:li [:a {:href "#/expungeable"} "Expunction Interview"]]
;      [:li [:a {:href "#/nearby-courthouses"} "Nearby Courts"]]
]
;     [switch-buttons logged-in? [user-name] [register-login]]
]]]))

;; ------------- End Navbar ----------------------

;;; --------------- Footer -----------------------
(defn footer []
  [:div.navbar-static-bottom
   [:div.container-fluid
    [:p [:i "Don't see what you're looking for?" [:a {:href "#/contact-info"} " Drop us a line!"] " This website is not a substitute for the advice of an attorney.  © Expunct 2016. "]]]
])

(defn splash-footer []
  [:div.splash-footer
   [:div.container-fluid
    [:p [:i "Don't see what you're looking for?" [:a {:href "#/contact-info"} " Drop us a line!"] " This website is not a substitute for the advice of an attorney.  © Expunct 2016. "]]]
])

;;; --------------- End Footer -----------------------
  

;;;; ------------- Bootstrap Carousel ----------------------


(defn carouseller []
  [:div.container
   [:div {:class "row"}
;    [:div ;{:class  "mtc"}
     [:div {:id "myCarousel" :class "carousel slide"}
      [:ol {:class "carousel-indicators carousel-indicators--thumbnails"}

       [:li {:data-target "#myCarousel" :data-slide-to "0" :class "active"}
        [:div.thumbnail
         [:img {:src "/img/legal7.png" :class "img-responsive"}]]]

       [:li {:data-target "#myCarousel" :data-slide-to "1"}
        [:div.thumbnail
         [:img {:src "/img/legal7.png" :class "img-responsive"}]]]

       [:li {:data-target "#myCarousel" :data-slide-to "2"}
        [:div.thumbnail
         [:img {:src "/img/legal7.png" :class "img-responsive"}]]]]

      [:div {:class "carousel-inner"}

       [:div {:class "active item"}
        [:img {:src "/img/write13.png" 
               :class "img-responsive center-block" 
               :width "25%"}]
        [:div {:class "carousel-caption"} 
         [:h3 "1. Write your petition."]
         [:p "We'll help guide you through the court form."]]]

       [:div {:class "item"}
        [:img {:src "/img/public6.png" 
               :class "img-responsive center-block" 
               :width "25%"}]
        [:div {:class "carousel-caption"} 
         [:h3 "2. File your petition with the court."]
         [:p "You can do this yourself, or we can do it for you."]]]


       [:div {:class "item"}
        [:img {:src "/img/package36.png" 
               :class "img-responsive center-block" 
               :width "25%"}]
        [:div {:class "carousel-caption"} 
         [:h3 "3. Have a process server give the right papers to your landlord."]
         [:p "We can take care of that for you."]]]


       [:a {:class "carousel-control fui-arrow-left left" 
            :href "#myCarousel" :data-slide "prev"}]
       [:a {:class "carousel-control fui-arrow-right right" 
            :href "#myCarousel" :data-slide "next"}]]]]])


(defn mounter []
  (js/$ (fn []
        (.carousel (js/$ "#myCarousel"))
        )))


(defn carousel []
  (reagent/create-class {:component-did-mount mounter
                         :reagent-render carouseller}))


;; ------------ End Carousel  ------------


;; ------------ Pagination ------------

; get current-page. get the href and assoc to that page "active"

(defn paginator []
  [:div {:class "pagination pagination-centered"}
   [:ul
    [:li {:class 
          (when (re-find #"#left-property" (.-hash js/location)) "active")}   
     [:a {:href "#left-property"} 1]]
    [:li {:class 
          (when (re-find #"#time-limit-page" (.-hash js/location)) "active")} 
     [:a {:href "#time-limit-page"} 2]]
    [:li {:class 
          (when (re-find #"#written-reason" (.-hash js/location)) "active")} 
     [:a {:href "#written-reason"} 3]]
    [:li {:class 
          (when (re-find #"#owe-rent" (.-hash js/location)) "active")} 
     [:a {:href "#owe-rent"} 4]]
    [:li {:class 
          (when (re-find #"#disagree-rent" (.-hash js/location)) "active")} 
     [:a {:href "#disagree-rent"} 5]]
    [:li {:class 
          (when (re-find #"#personal-details" (.-hash js/location)) "active")} 
     [:a {:href "#personal-details"} 6]]]])

;; ------------ End Paginator -----------



;; Tool-tipper.
(defn tool-tipper [link title]
   [:a {:href link
         :target "_blank"}
     [:span {:class "fui-question-circle" 
             :data-toggle "tooltip" 
             :title title
             :data-placement "right"}]])

(defn popover [ title]
     [:span {:class "fui-question-circle" 
             :data-toggle "tooltip" 
             :title title
             :data-placement "right"}])


;; --------- Toggler -------------
(defn toggle-class [a k class1 class2]
  (if (= (@a k) class1)
    (swap! a assoc k class2)
    (swap! a assoc k class1)))

(defn toggler []
  (let [state (atom {:btn-class "btn btn-info"})]
    (fn []
      [:div
       [:input {:class (@state :btn-class)
                :type "button"
                :value "Click me"
                :on-click #(toggle-class state :btn-class 
                                         "btn btn-info" "btn btn-danger")}]])))


(defn toggle-yes [yes-message]
  (let [state (atom {:secret-class "hidden-secret"})]
    (fn []
      [:div
       [:input {:class "btn btn-info palette-turquoise"
                :type "button"
                :value "Yes"
                :on-click #(toggle-class state :secret-class 
                                         "hidden-secret" "nothidden")}]
       [:span {:class (@state :secret-class)} yes-message]])))

(defn toggle-no [no-message]
  (let [state (atom {:secret-class "hidden-secret"})]
    (fn []
      [:div
       [:input {:class "btn btn-info palette-carrot"
                :type "button"
                :value "No"
                :on-click #(toggle-class state :secret-class 
                                         "hidden-secret" "nothidden")}]
       [:span {:class (@state :secret-class)} no-message]])))

;; ------------- End Toggler -------------- 



(defn- before
  "Return a new js/Date which is the given number of days before the given date"
  [date days]
  (js/Date. (.getFullYear date) (.getMonth date) (- (.getDate date) days)))

(defn date? [x]
  (= (type x) js/Date))


(defn days-between 
  "Return the number of days between the two js/Date instances"
  [x y]
  (when (every? date? [x y])
    (let [ms-per-day (* 1000 60 60 25)
          x-ms (.getTime x)
          y-ms (.getTime y)]
      (.round js/Math (.abs js/Math (/ (- x-ms y-ms) ms-per-day))))))


(def today (js/Date.))
(def start-date (atom today))
(def end-date (atom today))
(def total-days-selected (reaction (days-between @(subscribe [:date-moved-out]) today)))

(defn alt-picker 
  "Entries go to a multimethod. Requires a key as argument - :birthdate, 
  :arrest-date, :dismissal-date, or :forwarding-address."
  [date-type]
  [:div;.form-group
   [:div.input-group
     [:span {:class "input-group-btn"}
      [:button {:class "btn" :type "button"}
       [:span {:class "fui-calendar"}]]]

     [pikaday/date-selector 
      {:date-atom (subscribe [:date-moved-out])
       :max-date-atom end-date
       :date-type date-type
       :class "form-control"
       :type "text"
       :pikaday-attrs {:max-date today}}]]])


(defn alt-picker2 [day max-date]
  [:div.form-group
   [:div.input-group
     [:span {:class "input-group-btn"}
      [:button {:class "btn" :type "button"}
       [:span {:class "fui-calendar"}]]]

     [pikaday/date-selector2
      {:date-atom day
       :max-date-atom max-date
       :class "form-control"
       :type "text"
       :pikaday-attrs {:max-date today}}]]])


(defn alt-picker3 [day start-date end-date]
  [:div.parent-container

   [:div.child-column
    [:p "Start Date"]
    [:div.form-group
     [:div.input-group
      [:span {:class "input-group-btn"}
       [:button {:class "btn" :type "button"}
        [:span {:class "fui-calendar"}]]]
      [pikaday/date-selector2
       {:date-atom start-date
        :max-date-atom end-date
        :class "form-control"
        :type "text"
        :input-attrs {:id "start"}
        :pikaday-attrs {:max-date today}}]]]]

    [:div.child-column 
     [:p "End Date"]
     [:div.form-group
      [:div.input-group
       [:span {:class "input-group-btn"}
        [:button {:class "btn" :type "button"}
         [:span {:class "fui-calendar"}]]]
       [pikaday/date-selector2
        {:date-atom end-date
         :min-date-atom start-date
         :class "form-control"
         :type "text"
         :input-attrs {:id "end"}
         :pikaday-attrs {:max-date today}}]]]]])





;; --------- Sortable Portlets ---------
(declare nowhere)
(declare left-property)
(defn disabled-portlet-button [cause]
   [:div.portlet
    [:div.portlet-content (mymacros/disabled-button nowhere cause)]])

(defn ready-portlet-button [cause]
   [:div.portlet
    [:div.portlet-content (mymacros/primary-button left-property cause)]])



(defn portlet-page [ready-causes not-ready-causes]
  [:div
   [:h6 "Choose everything that looks like a problem worth going to court over."]

   [:div.portlet-column
    [:div.portlet [:p "Place here"]]]

   [:div.portlet-column
    (for [ready-cause ready-causes]
      ^{:key ready-cause}[ready-portlet-button ready-cause])]

   [:div.portlet-column
    (for [not-cause not-ready-causes]
      ^{:key not-cause} [disabled-portlet-button not-cause])]])

(def ready-causes ["Deposit Withheld"])

(def not-ready-causes ["Noisy Neighbor" 
                     "Rat Infestation" 
                     "Breach of Contract" 
                     "TDTPA" 
                     "Assault and/or Battery"])


(defn sortable-portlets []
  (js/$ (fn []
          (.sortable (js/$ ".portlet-column") 
                     (clj->js 
                      {:connectWith ".portlet-column"
                       :handle ".portlet-content"
                       :cancel ".portlet-toggle"
                       :placeholder "portlet-placeholder ui-corner-all"})))))


;; -------------------------
;; Sortable Portlets Key Function

(def display-messages {:arrest [[:arrest-county] [:enter-arrest-county]]
                       })

(defmethod init-field :typeahead
  [[type {:keys [id data-source input-class list-class item-class highlight-class result-fn choice-fn clear-on-focus?]
          :as attrs
          :or {result-fn identity
               choice-fn identity
               clear-on-focus? true}}] {:keys [doc get save!]}]
  (let [typeahead-hidden? (atom true)
        mouse-on-list? (atom false)
        selected-index (atom 0)
        selections (atom [])
        choose-selected #(do (let [choice (nth @selections @selected-index)]
                               (save! id choice)
                               (choice-fn choice)
                               (dispatch [:enter-arrest-county choice]))
                             (reset! typeahead-hidden? true))]
    (render-element attrs doc
                    [type
                     [:input {:type        :text
                              :class       input-class
                              :placeholder @(subscribe [:arrest-county])
                              :size         20
                              :value       (let [v (get id)]
                                             (if-not (iterable? v)
                                               v (first v)))
                              :on-focus    #(when clear-on-focus? (save! id ""))
                              :on-blur     #(when-not @mouse-on-list?
                                              (reset! typeahead-hidden? true)
                                              (reset! selected-index 0))
                              :on-change   #(do
                                              (reset! selections (data-source (.toLowerCase (value-of %))))
                                              (save! id (value-of %))
                                              (reset! typeahead-hidden? false)
                                              (reset! selected-index 0))
                              :on-key-down #(do
                                              (case (.-which %)
                                                38 (do
                                                     (.preventDefault %)
                                                     (if-not (= @selected-index 0)
                                                       (reset! selected-index (- @selected-index 1))))
                                                40 (do
                                                     (.preventDefault %)
                                                     (if-not (= @selected-index (- (count @selections) 1))
                                                       (reset! selected-index (+ @selected-index 1))))
                                                9  (do 
                                                     (.preventDefault %)
                                                     (choose-selected))
                                                ;; Enter key
                                                13 (do 
                                                     (.preventDefault %)
                                                     (choose-selected))
                                                27 (do (reset! typeahead-hidden? true)
                                                       (reset! selected-index 0))
                                                "default"))}]

                     [:ul {:style {:display (if (or (empty? @selections) @typeahead-hidden?) :none :block) }
                           :class list-class
                           :on-mouse-enter #(reset! mouse-on-list? true)
                           :on-mouse-leave #(reset! mouse-on-list? false)}
                      (doall
                       (map-indexed
                        (fn [index result]
                          [:li {:tab-index     index
                                :key           index
                                :class         (if (= @selected-index index) highlight-class  item-class)
                                :on-mouse-over #(do
                                                  (reset! selected-index (js/parseInt (.getAttribute (.-target %) "tabIndex"))))
                                :on-click      #(do
                                                  (reset! typeahead-hidden? true)
                                                  (save! id result)
                                                  (choice-fn result))}
                           (result-fn result)])
                        @selections))]])))

(defmethod init-field :typeahead-hack
  [[type {:keys [id data-source input-class list-class item-class highlight-class result-fn choice-fn clear-on-focus?]
          :as attrs
          :or {result-fn identity
               choice-fn identity
               clear-on-focus? true}}] {:keys [doc get save!]}]
  (let [typeahead-hidden? (atom true)
        mouse-on-list? (atom false)
        selected-index (atom 0)
        selections (atom [])
        choose-selected #(do (let [choice (nth @selections @selected-index)]
                               (save! id choice)
                               (choice-fn choice)
                               (dispatch [:enter-state choice]))
                             (reset! typeahead-hidden? true))]
    (render-element attrs doc
                    [type
                     [:input {:type        :text
                              :class       input-class
                              :placeholder @(subscribe [:state])
                              :size         20
                              :value       (let [v (get id)]
                                             (if-not (iterable? v)
                                               v (first v)))
                              :on-focus    #(when clear-on-focus? (save! id ""))
                              :on-blur     #(when-not @mouse-on-list?
                                              (reset! typeahead-hidden? true)
                                              (reset! selected-index 0))
                              :on-change   #(do
                                              (reset! selections (data-source (.toLowerCase (value-of %))))
                                              (save! id (value-of %))
                                              (reset! typeahead-hidden? false)
                                              (reset! selected-index 0))
                              :on-key-down #(do
                                              (case (.-which %)
                                                38 (do
                                                     (.preventDefault %)
                                                     (if-not (= @selected-index 0)
                                                       (reset! selected-index (- @selected-index 1))))
                                                40 (do
                                                     (.preventDefault %)
                                                     (if-not (= @selected-index (- (count @selections) 1))
                                                       (reset! selected-index (+ @selected-index 1))))
                                                9  (do 
                                                     (.preventDefault %)
                                                     (choose-selected))
                                                ;; Enter key
                                                13 (do 
                                                     (.preventDefault %)
                                                     (choose-selected))
                                                27 (do (reset! typeahead-hidden? true)
                                                       (reset! selected-index 0))
                                                "default"))}]

                     [:ul {:style {:display (if (or (empty? @selections) @typeahead-hidden?) :none :block) }
                           :class list-class
                           :on-mouse-enter #(reset! mouse-on-list? true)
                           :on-mouse-leave #(reset! mouse-on-list? false)}
                      (doall
                       (map-indexed
                        (fn [index result]
                          [:li {:tab-index     index
                                :key           index
                                :class         (if (= @selected-index index) highlight-class  item-class)
                                :on-mouse-over #(do
                                                  (reset! selected-index (js/parseInt (.getAttribute (.-target %) "tabIndex"))))
                                :on-click      #(do
                                                  (reset! typeahead-hidden? true)
                                                  (save! id result)
                                                  (choice-fn result))}
                           (result-fn result)])
                        @selections))]])))

(def US-states
["Alabama"
"Alaska"
"Arizona"
"Arkansas"
"California"
"Colorado"
"Connecticut"
"Delaware"
"Florida"
"Georgia"
"Hawaii"
"Idaho"
"Illinois"
"Indiana"
"Iowa"
"Kansas"
"Kentucky"
"Louisiana"
"Maine"
"Maryland"
"Massachusetts"
"Michigan"
"Minnesota"
"Mississippi"
"Missouri"
"Montana"
"Nebraska"
"Nevada"
"New Hampshire"
"New Jersey"
"New Mexico"
"New York"
"North Carolina"
"North Dakota"
"Ohio"
"Oklahoma"
"Oregon"
"Pennsylvania"
"Rhode Island"
"South Carolina"
"South Dakota"
"Tennessee"
"Texas"
"Utah"
"Vermont"
"Virginia"
"Washington"
"West Virginia"
"Wisconsin"
"Wyoming"
"District of Columbia"
"Puerto Rico"
"Guam"
"American Samoa"
"U.S. Virgin Islands"
"Northern Mariana Islands"])

(defn state-source [text]
  (filter
    #(-> % (.toLowerCase %) (.indexOf text) (> -1))
    US-states))

(def Texas-counties
["Anderson County"
"Andrews County"
"Angelina County"
"Aransas County"
"Archer County"
"Armstrong County"
"Atascosa County"
"Austin County"
"Bailey County"
"Bandera County"
"Bastrop County"
"Baylor County"
"Bee County"
"Bell County"
"Bexar County"
"Blanco County"
"Borden County"
"Bosque County"
"Bowie County"
"Brazoria County"
"Brazos County"
"Brewster County"
"Briscoe County"
"Brooks County"
"Brown County"
"Burleson County"
"Burnet County"
"Caldwell County"
"Calhoun County"
"Callahan County"
"Cameron County"
"Camp County"
"Carson County"
"Cass County"
"Castro County"
"Chambers County"
"Cherokee County"
"Childress County"
"Clay County"
"Cochran County"
"Coke County"
"Coleman County"
"Collin County"
"Collingsworth County"
"Colorado County"
"Comal County"
"Comanche County"
"Concho County"
"Cooke County"
"Coryell County"
"Cottle County"
"Crane County"
"Crockett County"
"Crosby County"
"Culberson County"
"Dallam County"
"Dallas County"
"Dawson County"
"De Witt County"
"Deaf Smith County"
"Delta County"
"Denton County"
"Dickens County"
"Dimmit County"
"Donley County"
"Duval County"
"Eastland County"
"Ector County"
"Edwards County"
"El Paso County"
"Ellis County"
"Erath County"
"Falls County"
"Fannin County"
"Fayette County"
"Fisher County"
"Floyd County"
"Foard County"
"Fort Bend County"
"Franklin County"
"Freestone County"
"Frio County"
"Gaines County"
"Galveston County"
"Garza County"
"Gillespie County"
"Glasscock County"
"Goliad County"
"Gonzales County"
"Gray County"
"Grayson County"
"Gregg County"
"Grimes County"
"Guadalupe County"
"Hale County"
"Hall County"
"Hamilton County"
"Hansford County"
"Hardeman County"
"Hardin County"
"Harris County"
"Harrison County"
"Hartley County"
"Haskell County"
"Hays County"
"Hemphill County"
"Henderson County"
"Hidalgo County"
"Hill County"
"Hockley County"
"Hood County"
"Hopkins County"
"Houston County"
"Howard County"
"Hudspeth County"
"Hunt County"
"Hutchinson County"
"Irion County"
"Jack County"
"Jackson County"
"Jasper County"
"Jeff Davis County"
"Jefferson County"
"Jim Hogg County"
"Jim Wells County"
"Johnson County"
"Jones County"
"Karnes County"
"Kaufman County"
"Kendall County"
"Kenedy County"
"Kent County"
"Kerr County"
"Kimble County"
"King County"
"Kinney County"
"Kleberg County"
"Knox County"
"La Salle County"
"Lamar County"
"Lamb County"
"Lampasas County"
"Lavaca County"
"Lee County"
"Leon County"
"Liberty County"
"Limestone County"
"Lipscomb County"
"Live Oak County"
"Llano County"
"Loving County"
"Lubbock County"
"Lynn County"
"Madison County"
"Marion County"
"Martin County"
"Mason County"
"Matagorda County"
"Maverick County"
"McCulloch County"
"McLennan County"
"McMullen County"
"Medina County"
"Menard County"
"Midland County"
"Milam County"
"Mills County"
"Mitchell County"
"Montague County"
"Montgomery County"
"Moore County"
"Morris County"
"Motley County"
"Nacogdoches County"
"Navarro County"
"Newton County"
"Nolan County"
"Nueces County"
"Ochiltree County"
"Oldham County"
"Orange County"
"Palo Pinto County"
"Panola County"
"Parker County"
"Parmer County"
"Pecos County"
"Polk County"
"Potter County"
"Presidio County"
"Rains County"
"Randall County"
"Reagan County"
"RealRed River County"
"Reeves County"
"Refugio County"
"Roberts County"
"Robertson County"
"Rockwall County"
"Runnels County"
"Rusk County"
"Sabine County"
"San Augustine County"
"San Jacinto County"
"San Patricio County"
"San Saba County"
"Schleicher County"
"Scurry County"
"Shackelford County"
"Shelby County"
"Sherman County"
"Smith County"
"Somervell County"
"Starr County"
"Stephens County"
"Sterling County"
"Stonewall County"
"Sutton County"
"Swisher County"
"Tarrant County"
"Taylor County"
"Terrell County"
"Terry County"
"Throckmorton County"
"Titus County"
"Tom Green County"
"Travis County"
"Trinity County"
"Tyler County"
"Upshur County"
"Upton County"
"Uvalde County"
"Val Verde County"
"Van Zandt County"
"Victoria County"
"Walker County"
"Waller County"
"Ward County"
"Washington County"
"Webb County"
"Wharton County"
"Wheeler County"
"Wichita County"
"Wilbarger County"
"Willacy County"
"Williamson County"
"Wilson County"
"Winkler County"
"Wise County"
"Wood County"
"Yoakum County"
"Young County"
"Zapata County"
"Zavala County"])

(defn Texas-county-source [text]
  (filter
    #(-> % (.toLowerCase %) (.indexOf text) (> -1))
    Texas-counties))

(defn row [label input]
  [:div.row
    [:div.col-md-2 [:label label]]
    [:div.col-md- input]])

(defmulti form-template (fn [keytype] keytype))
(defmethod form-template :Texas-counties []
  [:div {:field           :typeahead
         :id              "typer"
         :data-source     Texas-county-source
         :input-class     "form-control"
         :list-class      "typeahead-list"
         :item-class      "typeahead-item"
         :highlight-class "highlighted"}])

(defmethod form-template :states []
  [:div {:field           :typeahead-hack
         :id              "typer"
         :data-source     state-source
         :input-class     "form-control"
         :list-class      "typeahead-list"
         :item-class      "typeahead-item"
         :highlight-class "highlighted"}])


(defn typeahead [data-source]
  (let [doc (atom {})]
    [:div
     [bind-fields
      (form-template data-source)
      doc]]))


;;;;;;;;;; Progress Bar;;;;;;;;;;;

(defn compute-strength [password]  
    (str (aget (js/zxcvbn @password) "score")))

(defn progress-bar [password]
  (let [strength (compute-strength password)]
  [:div.progress
   [:div.progress-bar.progress-bar-danger 
    {:role "progressbar" :style {:width "15%"}}]
   (when (>= strength password 1)
   [:div.progress-bar.progress-bar-carrot {:style {:width "25%"}}])
   (when (>= strength 2)
   [:div.progress-bar.progress-bar-warning {:style {:width "25%"}}])
   (when (>= strength 3)
   [:div.progress-bar {:style {:width "30%"}}])
   (when (>= strength 4)
   [:div.progress-bar.progress-bar-success {:style {:width "30%"}}])]))

;;;;;;;;  End Progress Bar ;;;;;;;;;;;

(defn check-nil-then-predicate
  "Check if the value is nil, then apply the predicate"
  [value predicate]
  (if (nil? value)
    false
    (predicate value)))

;; Regex for a valid email address
(def email-regex #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
;(def email-regex #".+\@.+\..+")

(defn valid-email?
  "Determine if the specified email address is valid according to our email regex."
  [email]
  (let [email @email]
  (and (not (nil? email)) (re-matches email-regex email))))


(defn eight-or-more-characters?
  [word]
  (check-nil-then-predicate word (fn [arg] (> (count arg) 7))))


(defn has-special-character?
  [word]
  (check-nil-then-predicate word (fn [arg] (boolean (first (re-seq #"\W+" arg))))))


(defn has-number?
  [word]
  (check-nil-then-predicate word (fn [arg] (boolean (re-seq #"\d+" arg)))))


(defn prompt-message
  "A prompt that will animate to help the user with a given input"
  [message]
  [:div {:class "my-messages"}
   [:div {:class "prompt message-animation"} [:p message]]])


(defn password-input-element
  "An input element which updates its value and on focus parameters on change, blur, and focus"
  [id name type password in-focus]
  [:input {:id id
           :name name
           :class "form-control"
           :type type
           :required ""
           :placeholder @password
           :on-change #(reset! password (-> % .-target .-value))
           :on-focus #(swap! in-focus not)
           :on-blur #(swap! in-focus not)}])

(defn email-input-element
  "An input element which updates its value and on focus parameters on change, blur, and focus"
  [id name type value in-focus]
  [:input {:id id
           :name name
           :class "form-control"
           :type type
           :required ""
           :placeholder @value
           :on-change #(dispatch [:enter-your-email (-> % .-target .-value)])
;           :on-focus #(swap! in-focus not)
           :on-blur (fn [arg] (if (nil? @value) (reset! value ""))(swap! in-focus not))}])

(defn email-input
  "An input element which updates its value and on focus parameters on change, blur, and focus"
  []
  (let [value (subscribe [:your-email])
        in-focus (atom false)]
  [:input {:name name
           :class "form-control"
           :type "email"
           :required ""
           :placeholder @value
           :on-change #(dispatch [:enter-your-email (-> % .-target .-value)])
;           :on-focus #(swap! in-focus not)
           :on-blur (fn [arg] (if (nil? @value) (reset! value ""))(swap! in-focus not))}]))



(defn input-and-prompt
  "Creates an input box and a prompt box that appears above the input when the input comes into focus. Also throws in a little required message"
  [input-element label-value input-name input-type input-element-arg prompt-element required?]
  (let [input-focus (atom false)]
    (fn []
      [:div
       [input-element input-name input-name input-type input-element-arg input-focus]
       (if (and required? (= "" @input-element-arg))
         [:div "Field is required!"]
         [:div])])))

(defn password-input-and-prompt
  "Creates an input box and a prompt box that appears above the input when the input comes into focus. Also throws in a little required message"
  [input-element label-value input-name input-type password]
  (let [input-focus (atom false)]
    (fn []
      [:div
       [input-element input-name input-name input-type password input-focus]
       (when @input-focus 
         [:div
          [:br]
         [progress-bar password]])])))

(defn email-form [email-address-atom]
  (input-and-prompt email-input-element
                    "email"
                    "email"
                    "email"
                    email-address-atom
                    (prompt-message "What's your email address?")
                    true))


(defn password-requirements
  "A list to describe which password requirements have been met so far"
  [password requirements]
  [:div
   [:ul (->> requirements
             (filter (fn [req] (not ((:check-fn req) password))))
             (doall)
             (map (fn [req] ^{:key req} [:li (:message req)])))]])


(defn password-form
  [password]
    (fn []
      [:div
       [(password-input-and-prompt password-input-element
                          "password"
                          "password"
                          "password"
                          password)]]))

(defn second-password-form
  [password]
    (fn []
      [:div
       [(input-and-prompt password-input-element
                          "password"
                          "password"
                          "password"
                          password
                          "hello"
                          true)]]))


(defn wrap-as-element-in-form
  [element]
  [:div {:class "row form-group"}
   [:div
    [:form {:class "form-inline" :role "form"}
     [:div {:class "form-group"}
      element]]]])


(defn change-legal-name []
  "Taking the first and last name, this creates a default legal name."
  (let [firstname (subscribe [:first-name])
        lastname (subscribe [:last-name])]
    (dispatch [:enter-yourname (str @firstname " " @lastname)])))

(defn register-button [password] 
  (fn []
    [:input {:type "button" 
             :class "btn btn-info" 
             :value "Do it"
             :on-click #(do (change-legal-name)
                            (register-user @password)
                            (reagent-modals/close-modal!)
                            )}]))

(defn login-button [password forgot-password] 
  (fn []
    [:input {:type "button" 
             :class "btn btn-alizarin" 
             :value "Do it"
             :on-click #(login-user @password forgot-password)}]))


(defn update-password-button [password] 
  (fn []
    [:input {:type "button" 
             :class "btn btn-alizarin" 
             :value "Do it"
             :on-click #(new-password @password)}]))


(defn email-wait-button [] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value "Register"
           :on-click #(js/alert "Sorry, we need your email!")}]))

(defn password-mismatch-button [value] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value value
           :on-click #(js/alert "Sorry, your passwords don't match!")}]))

(defn bad-password-button [value] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value value
           :on-click #(js/alert "Please make sure your password has eight 
characters, and a special character or a number.")}]))

(defn need-name-button [] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-oranj" 
           :value "Register"
           :on-click #(js/alert "Please enter your first and last names.")}]))

(defn good-password? [password]
  (let [password @password]
  (and
   (eight-or-more-characters? password)
   (or (has-special-character? password)
       (has-number? password)))))

(defn password-match? [password second-password]
  (= @password @second-password))

(defn name-filled? []
    (and
     (not= "your first name" @(subscribe [:first-name]))
     (not= "your last name" @(subscribe [:last-name]))))

(defn complete-credentials? [password second-password email-address]
  (and 
   (valid-email? email-address)
   (good-password? password)
   (name-filled?)))

(defn legalname-filled? []
  (not= "Full legal name" @(subscribe [:yourname])))

(defn email-filled? []
  (let [email-address (subscribe [:your-email])]
    (valid-email? email-address)))

(defn phone-filled? []
  (let [number @(subscribe [:your-phone])]
    (not= "Your phone number" number)))

(defn arrest-county-filled? []
  (let [county @(subscribe [:arrest-county])]
    (not= "County of arrest" county)))

(defn birthdate-filled? []
  (let [birthdate @(subscribe [:birthdate])]
    (not= "Date of birth" birthdate)))


(defn arrestdate-filled? []
  (let [arrestdate @(subscribe [:arrest-date])]
    (not= "Date of arrest" arrestdate)))

(defn complete-form? []
  (and
   (legalname-filled?)
   (phone-filled?)
   (email-filled?)
   (birthdate-filled?)
   (arrestdate-filled?)
   (arrest-county-filled?)
   ))

(defn show-validation-status! [nameclass phoneclass emailclass birthclass adateclass countyclass]
  (do
    (if (legalname-filled?)
      (reset! nameclass "has-success")
      (reset! nameclass "has-error"))

    (if (phone-filled?) 
      (reset! phoneclass "has-success")
      (reset! phoneclass "has-error"))

    (if (email-filled?)
      (reset! emailclass "has-success")
      (reset! emailclass "has-error"))

    (if (birthdate-filled?)
      (reset! birthclass "has-success")
      (reset! birthclass "has-error"))

    (if (arrestdate-filled?)
      (reset! adateclass "has-success")
      (reset! adateclass "has-error"))

    (if (arrest-county-filled?)
      (reset! countyclass "has-success")
      (reset! countyclass "has-error"))))

(defn go? [showme nameclass phoneclass emailclass birthclass adateclass countyclass]
  (if (complete-form?)
    (.-preventDefault (reset! showme "b"))
    (show-validation-status! nameclass phoneclass emailclass birthclass adateclass countyclass)))

(defn card-button [showme nameclass phoneclass emailclass birthclass adateclass countyclass]
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Checkout"
           :on-click #(go? showme nameclass phoneclass emailclass birthclass adateclass countyclass)}]))

(defn continue-button [showme nameclass phoneclass emailclass birthclass adateclass countyclass]
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Continue"
           :on-click #(go? showme nameclass phoneclass emailclass birthclass adateclass countyclass)}]))


(defn register-switch-buttons [password second-password email-address]
  (fn []
    [:div
     (cond
       (not (name-filled?)) [need-name-button]
       (not (valid-email? email-address)) [email-wait-button]
       (not (good-password? password)) [bad-password-button "Register"]
       (not (password-match? password second-password)) [password-mismatch-button "Register"]
       :else [register-button password])]))


(defn switch-buttons2 [atom default alternative]
  (fn []
    (if-not @atom
      default
      alternative)))

(defn password-switch-buttons [password second-password]
  (fn []
    [:div
     (cond
       (not (good-password? password)) [bad-password-button "Update"]
       (not (password-match? password second-password)) [password-mismatch-button "Update"]
       :else [update-password-button password])]))

(defn login-switch-buttons [password email-address forgot-password]
  (fn []
    [:div
     (if (valid-email? email-address)

       [login-button password forgot-password]
       [wait-button])]))

(defn beeminder-graph []
  [:div
   [:iframe {:src "https://www.beeminder.com/widget?slug=dailyuvi&username=sensei&countdown=true"
             :height "245px"
             :width "230px"
             :frameborder "0px"}]])

;;;; HTML5 LocalStorage Utility Functions
(defn set-item!
  "Set `key' in browser's localStorage to `val`."
  [key val]
  (.setItem (.-localStorage js/window) key val))

(defn get-item
  "Returns value of `key' from browser's localStorage."
  [key]
  (.getItem (.-localStorage js/window) key))

(defn remove-item!
  "Remove the browser's localStorage value for the given `key`"
  [key]
  (.removeItem (.-localStorage js/window) key))


;;;;;;;;;;;;;;;;; Stripe Components ;;;;;;;;;;;;;;;;;;;;;;;;;

;;;; Functions to find the anti-forgery-token
(defn anti-forgery-token []
  (-> :#anti-forgery-token
          sel1))
(defn token []
  (ef/from (anti-forgery-token) (ef/get-attr :value)))
;;;; ----------- End anti-forgery-token


(defn- insert-before-selector [selector tag]
  (let [first-tag (.querySelector js/document selector)]
    (.insertBefore (.-parentNode first-tag) tag first-tag)))

(def insert-body-bottom (partial insert-before-selector "script"))
(def insert-in-head (partial insert-before-selector "head link"))


(defn src-tag
  [src class]
  (let [script-tag (.createElement js/document "script")]
    (set! (.-type script-tag) "text/javascript")
    (set! (.-async script-tag) "true")
    (set! (.-src script-tag) src)
    (classlist/add script-tag class)
    script-tag))


(defn insert-tag-with-callback [tag callback]
  (set! (.-onload tag) callback)
  (insert-body-bottom tag))

(defn insert-stripe []
  (insert-tag-with-callback
   (src-tag "https://js.stripe.com/v2/stripe.js" "stripe")
   (fn []  (fn [] (js/Stripe.setPublishableKey "pk_test_fltZYExKAaJKHJlUwj9WAvXw"))))

(defn validate-card [card month year cvc]
  (let [good-card (Stripe.card.validateCardNumber @card)
        valid-date (Stripe.card.validateExpiry @month @year)
        valid-cvc (Stripe.card.validateCVC @cvc)]
  (cond 
    (and good-card valid-date valid-cvc) true
    (not good-card) "Please enter a valid card number."
    (not valid-date) "Please enter a valid expiration date."
    (not valid-cvc) "Please enter a valid security code.")))


(defn stripey [card name month year cvc message amount showme]
  (let [response-chan (chan)
        email (subscribe [:your-email])
        name (subscribe [:yourname])
        phone (subscribe [:your-phone])
        birthdate (subscribe [:birthdate])
        arrest-date (subscribe [:arrest-date])]
;    (js/google_trackConversion)
    (js/Stripe.card.createToken (clj->js {:number @card
                                          :cvc @cvc
                                          :exp-month @month
                                          :exp-year @year})
                             #(put! response-chan (js->clj %& :keywordize-keys true)))
    (go
      (let [response (second (<! response-chan))
            postme (<! (http/post "/checkout" {:transit-params {:id (:id response)
                                                                :email @email
                                                                :amount amount
                                                                :name @name
                                                                :phone @phone
                                                                :birthdate @birthdate
                                                                :arrest-date @arrest-date}
                                               :headers {"x-csrf-token" (token)}}))
            response-message (:body postme)
            server-error (= 500 (:status postme))
            success (= 200 (:status postme))]
        (reset! message response-message)
        (if success
          (do
            (dispatch [:enter-expunction-purchase "It's on the way."])
            (reset! showme "c")))
))))

(defn validatum [card name month year cvc message amount showme]
  (let [validated? (validate-card card month year cvc)]
    (if (= true validated?)
      (do
        (reset! message "Just one moment...")
        (stripey card name month year cvc message amount showme))
      (reset! message validated?))))

(defn payment-input [class target]
  [:input.checkout-input {:class class
                          :type "text"
                          :placeholder @target
                          :on-change #(reset! target (-> % .-target .-value))}])

(defn payment-information [amount showme]
  (let [name (atom "Your name")
        month (atom "MM")
        year (atom "YY")
        card (atom "4242 4242 4242 4242")
        cvc (atom "CVC")
        message (atom "")]
    (fn []
  [:div.checkout
   [:div.checkout-header
    [:h1.checkout-title "Checkout"
    [:span.checkout-price (str "$" (/ amount 100))]]]
   [:p
    [payment-input "checkout-name" name]
    [payment-input "checkout-exp" month]
    [payment-input "checkout-exp" year]]
   [:p
    [payment-input "checkout-card" card]
    [payment-input "checkout-cvc" cvc]]
   [:p
    [:input {:type "button" 
             :class "checkout-btn" 
             :value "Purchase"
             :on-click #(validatum card name month year cvc message amount showme)}]
    [:div @message]
    ]])))


(defn purchase-button [amount]
    (fn []
      [:input {:type "button" 
               :class "btn btn-danger" 
               :value "Enter payment details"
               :on-click #(reagent-modals/modal! [payment-information amount])
               }]))


(defn progression [showme a b c]
  (fn []
  [:div
   (cond
     (= @showme "a") a
     (= @showme "b") b
     (= @showme "c") c)]))


(defn finish-register []
  (let [password (atom "Secr3t P@ssword!")
	second-password (atom "confirm password")
        email-address (subscribe [:your-email])]
  [:div
   [:div.modal-header
    [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
    [:h6.modal-title "Congratulations, your Record Recover is on the way!"]
    [:p "Please choose a password to finish creating your account."]]
   [:div.modal-body
    [:form.form-horizontal {:role "form"}

     [:div.form-group
      [:div.col-xs-8
       [password-form password]]]

     [:div.form-group
      [:div.col-xs-8
       [second-password-form second-password]]]]]     
   [:div.modal-footer
    [register-switch-buttons password second-password email-address]]]))

(defn lookup-finish []
  (fn []
  [:div
   [:div.modal-header
    [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
    [:h6.modal-title "Congratulations, your free background check is on the way!"]
    [:p "We'll let you know what we find."]]
   [:div.modal-body
    [:p "Check out our FAQs, and contact us with any questions."]
    [:p "By the way, if you send a friend and they clear their record with us, we'll give you $50 as a thank you. :-)"]]
   [:div.modal-footer
    [:input {:type "button" 
             :class "btn btn-info" 
             :value "Close"
             :on-click #(reagent-modals/close-modal!)}]]]))


(defn background-lookup [showme]
    (fn []
  [:div
   [:div.modal-header
    [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
    [:h6.modal-title "Almost there!"]]
   [:div.modal-body
    [:p "Please remember that Texas law allows you to look up yourself, but no one else."]
    [:p "By submitting this request, you're representing that you're the person you're looking up."]]
    
   [:div.modal-footer
    [:p "Good to go?"]
    [:form.form-horizontal {:role "form"}
      [:div.form-group
       [:input {:type "button" 
             :class "btn btn-info" 
             :value "Back"
             :on-click #(.-preventDefault (reset! showme "a"))}]


       [:input {:type "button" 
             :class "btn btn-danger" 
             :value "Submit"
             :on-click #(do
                          (.-preventDefault (reset! showme "c"))
                          (lookup-record :welcome))}]]]
]]))

(defn edit-info [showme]
  (let [nameclass (atom "")
        phoneclass (atom "")
        emailclass (atom "")
        birthclass (atom "")
        adateclass (atom "")
        countyclass (atom "")]
    (fn []
  [:div
   [:div.modal-header
    [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
    [:h6.modal-title "We need some personal information to look you up."]]
   [:div.modal-body
    [:form.form-horizontal {:role "form"}
     
      [:div.form-group
       [:div#legalname.col-xs-8 {:class @nameclass} [medium-text-box :yourname :enter-yourname]]
       [:label.col-xs-4 {:for "legalname"} [:i "Name "]]]

      [:div.form-group
       [:div#phone.col-xs-8 {:class @phoneclass} [medium-text-box :your-phone :enter-your-phone]]
       [:label.col-xs-4 [:i "Phone"]]]

      [:div.form-group
       [:div#email.col-xs-8 {:class @emailclass} [medium-text-box :your-email :enter-your-email]]
       [:label.col-xs-4 {:for "email"} [:i "Your email "]]]

      [:div.form-group
       [:div#birthdate.col-xs-8 {:class @birthclass} [alt-picker :birthdate]]
       [:label.col-xs-4 {:for "birthdate"} [:i "Birthdate"]]]

      [:div.form-group
       [:div#arrest-date.col-xs-8 {:class @adateclass} [alt-picker :arrest-date]]
       [:label.col-xs-4 {:for "arrest-date"} [:i "Arrest date"]]]

      [:div.form-group
       [:div#arrest.col-xs-8 {:class @countyclass}
        [typeahead :Texas-counties]]
       [:label.col-xs-4 {:for "arrest"} [:i "Location"]]]
     [:div.form-group
      [:p [:i "We'll never share this."]]]
      ]]
   [:div.modal-footer
    [:div.form-horizontal
     [:div.col-xs-4
      [continue-button showme nameclass phoneclass emailclass birthclass adateclass countyclass]]
     [:div.col-xs-8
      [:p "By clicking Continue, you agree to the "
       [linky-button "terms and conditions " #(reagent-modals/modal! [tc-modal])]
       "and the " [linky-button " privacy policy." #(reagent-modals/modal! [privacy-modal])]]]]

]])))



(defn public-purchase-button [showme]
  [:div
   [reagent-modals/modal-window]
   [:input {:type "button" 
            :class "btn btn-hg btn-primary"
            :value "See what's on your record"
            :on-click #(reagent-modals/modal! 
                        [progression showme 
                         [edit-info showme] 
                         [background-lookup showme]
                         [lookup-finish]
                         {:size :md}])}]])


(defn poll-success 
  "This function checks for a valid email address, and then emails us the state and email address."
  [showme stateclass emailclass]
  (do
    ;; Add an explanatory message for the email-us function to send
    (dispatch-sync [:enter-contact (str "This " @(subscribe [:state]) " user would like us to come to their state. This is not a contact message. We should just put this email and state in a spreadsheet.")])
    ;; Change color to success
    (reset! stateclass "has-success")
    (reset! emailclass "has-success")
    ;; Send us and the potential customer an email
    (email-us :state-poll)
    (go
      ;; Show a different div
      (.-preventDefault (reset! showme "b"))
      ;; Wait 3 seconds
      (<! (timeout 3000))
    ;; close Modal window
      (reagent-modals/close-modal!))
    (dispatch [:enter-contact "Your question here"])
))

(defn poll-button [showme stateclass emailclass]
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Vote"
           :on-click #(if (email-filled?)
                        (poll-success showme stateclass emailclass)
                        (reset! emailclass "has-error"))}]))


(defn state-poll-form [showme]
  (let [stateclass (atom "")
        emailclass (atom "")]
    (fn []
  [:div
   [:div.modal-header
    [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
    [:h6.modal-title "Tell us where you are!"]]
   [:div.modal-body
    [:form.form-horizontal {:role "form"}
      [:div.form-group
       [:div#state.col-xs-8 {:class @stateclass} [typeahead :states]]
       [:label.col-xs-4 {:for "state"} [:i "Your state"]]]

      [:div.form-group
       [:div#email.col-xs-8 {:class @emailclass} [medium-text-box :your-email :enter-your-email]]
       [:label.col-xs-4 {:for "email"} [:i "Your email "]]]]]

   [:div.modal-footer
    [poll-button showme stateclass emailclass]]])))

(defn state-poll-thank-you []
  (fn []
    [:div
     [:div.modal-header
      [:button {:type "button" :class "close" :data-dismiss "modal" :aria-hidden "true"} "x"]
      [:h6.modal-title "Thank you!"]]
     [:div.modal-body
      [:h7 "We'll try to get to your state soon, and we'll let you know when we do."]
      [:h7 " Please let us know if you have any questions!"]]]))

(defn state-poll [showme]
  [:div
   [reagent-modals/modal-window]
       [:input {:type "button" 
             :class "btn btn-danger" 
             :value "Vote"
             :on-click #(reagent-modals/modal! 
                         [progression showme
                         [state-poll-form showme]
                          [state-poll-thank-you]
                          [:div]
                         {:size :sm}])}]])
