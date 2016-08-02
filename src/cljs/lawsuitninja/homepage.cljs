(ns lawsuitninja.homepage
  (:require   [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [lawsuitninja.components :refer [navbar carousel alt-picker 
                                               tool-tipper
                                               medium-text-box
                                               splash-footer
                                               insert-stripe
                                               public-purchase-button]]
              [reagent-modals.modals :as reagent-modals]
              [dommy.core :refer-macros [sel sel1]]
              [enfocus.core :as ef]
              [cljs-http.client :as http]
              [re-com.core :refer [v-box radio-button h-box box]]
              [re-frame.core :refer [register-handler
                                     path
                                     register-sub
                                     dispatch
                                     dispatch-sync
                                     subscribe]]

              [lawsuitninja.firstinterview :refer [firstinterview]]))

(defn six-steps []
  [:div.container
   [:div.row

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "3. We write and file your petition."]
     [:p "Let us take care of the paperwork."]
     [:img {:src "/img/write13.png" :class "center-block" :width "10%" :alt "legal file"}]
     [:br]]

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "2. We confirm you're eligible."]
     [:p "Go through our online interview to start."]
     [:img {:src "/img/public6.png" :class "center-block" :width "10%" :alt "courthouse"}]
     [:br]]

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "1. We look up your record."]
     [:p "We get a background check, and your file."]
     [:img {:src "/img/documents7.png" :class "center-block" :width "10%" :alt "legal paperwork"}]
     [:br]]]

   [:div.row
    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "6. We file the order."]
     [:p "And the government clears your record."]
     [:img {:src "/img/package36.png" :class "center-block" :width "10%" :alt "legal order"}]
     [:br]]

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "5. The Judge issues the order."]
     [:p "If not, your money back."]
     [:img {:src "/img/legal7.png" :class "center-block" :width "10%" :alt "courtroom judge"}]
     [:br]]

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "4. You attend your court date."]
     [:p "We'll see if you can avoid court costs."]
     [:img {:src "/img/business60.png" :class "center-block" :width "10%" :alt "court appearance"}]
     [:br]]]])


(defn three-steps []
  [:div.container
   [:div.row

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "3. Attend your court date."]
     [:p "We'll tell you when. Don't worry, it's not scary."]
     [:img {:src "/img/write13.png" :class "center-block" :width "10%" :alt "legal file"}]
     [:br]]

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "2. Open an account with us."]
     [:p "Give us your info and pay. Expunct gets to work right away."]
     [:img {:src "/img/public6.png" :class "center-block" :width "10%" :alt "courthouse"}]
     [:br]]

    [:div.col-xs-4.col-sm-4.col-md-4.col-lg-4
     [:h6 "1. Check out your record."]
     [:p "Instantly find out if it's the kind of record Expunct can help with."]
     [:img {:src "/img/documents7.png" :class "center-block" :width "10%" :alt "legal paperwork"}]
     [:br]]]

])



(defn start-button [page]
   [:a {:href page :type "button" 
        :class "btn btn-hg btn-primary"} "Check out your record (free)"])


(defn record-recover []
  [:div
   [:h5.form-centered "What's Record Recover?"]
   [:ul
    [:li "We start all of our expunctions by getting your actual police or court file."]
    [:li "Record Recover is the best way to know if you can get an expunction."]
    [:li "Record Recover includes a free background check."]]])

(defn guarantee []
  [:div
   [:h5.form-centered "What's the Court Cost Guarantee?"]
   [:ul
    [:li "If you pay us to clear your record, we will clear that record." ]
    [:li "If we can't, we'll refund your money, and also give you the money you paid
to the court." ]
    [:li "That's our Court Cost Guarantee."]]])


(defn record-tooltip []
  (fn []
    [:span {:class "fui-question-circle"
            :on-click #(reagent-modals/modal! [record-recover])
            :data-toggle "tooltip" 
            :title "What's Record Recover?"
            :data-placement "right"}]))

(defn guarantee-tooltip []
  (fn []
    [:span {:class "fui-question-circle"
            :on-click #(reagent-modals/modal! [guarantee])
            :data-toggle "tooltip" 
            :title "What's the Court Cost Guarantee?"
            :data-placement "right"}]))


(defn home-page []
;  (insert-stripe)
  (let [showme (atom "a")]

  (fn []
    [:div;.Site

     [:div.wrapper
     [:div.splashyheader]
      
      [:div#splashid.splashytitle.form-centered
       [:p.grande.splash-title "Expunct"]
       [:h3.cloudy "Clear your record for only $200"]
       [:div [start-button "#/recordrecover"]]]]

      [:div.splash-content
       [:div.page-header
        [navbar]
       [:div.form-centered
        [:h5 [:i "Expunct"]]
        [:ul
         [:li "If you're eligible for our help, we charge just $200 + court fee"]
         [:li "That's 90% off the cost of a lawyer, and we'll see if the court will waive its fee."]]]]
      [:div
       [:br]
       [:div.form-centered
        [:h6 "Expunge your record with our unique Court Cost Guarantee"
         "\u2122" ". " 
         [guarantee-tooltip]
         ]
        [:p "If we help you file an expunction, it works, or we'll refund our charges, and cover the court costs too."]
        [:p [:b "Here's how we do it."]]]
       [three-steps]

       [:br]
       [:div.form-centered 
        [:p [:i "Get started with a free background check."]]
        [public-purchase-button showme]
]]
       [:br]
     [splash-footer]]])))
