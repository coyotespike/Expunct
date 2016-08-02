(ns lawsuitninja.about
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
;   [cljsjs.react :as react]
   [clojure.string :as s]
   [goog.dom :as dom]
   [dommy.core :refer-macros [sel sel1]]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [lawsuitninja.components :refer [navbar state-poll beeminder-graph footer splash-footer]])
  (:import goog.History)
  (:require-macros [reagent.ratom :refer [reaction]]
                   ))


(defn about-page []
  (let [showme (atom "a")]
  [:div.Site
   [:div.wrapper
   [:div.page-header
    [navbar]]
   [:div.form-centered 
    [:h6 "About and Frequently Asked Questions"]]
   [:ol
    [:a {:href "#whofor"}    [:li "Who is this website for?"]]
    [:a {:href "#shouldi"}   [:li "Why should I get my record expunged?"]]
    [:a {:href "#cost"}      [:li "How much does an expunction cost?"]]
    [:a {:href "#guarantee"} [:li "What is the Court Cost Guarantee?"]]
    [:a {:href "#attend"}    [:li "Do I need to go to court?"]]
    [:a {:href "#lawyers"}   [:li "Are you my lawyers?"]]
    [:a {:href "#friend"}   [:li "What if I know someone else who could clear their record?"]]
   [:a {:href "#whoyou"}    [:li "Who are you guys and why are you doing this?"]]
]
    [:div.container
    [:div.row
     [:div.col-lg-1]
     [:div.col-lg-10
   [:ol
    [:li [:a {:name "whofor"}] [:b "Who is this website for?"]]
    [:p "Right now, Expunct is focused on people in Texas - let us know where you're from
       if you need our help (vote below). We'll expand soon!"]
    [state-poll showme]
    [:li [:a {:name "shouldi"}] [:b "Why should I get my record expunged?"]]
    [:p "The longer you wait to get an expunction, the more opportunities you will miss.
        Employers routinely conduct background checks on applicants, and on employees
        they're thinking about promoting."]
    [:p "Even if you were just arrested, never convicted, that is on your record!"]
    [:p "In addition, entire career fields such as nursing
        are closed to people with a criminal record. Getting an expunction is an investment which pays off many times over."]

    [:li [:a {:name "cost"}] [:b "How much does an expunction cost?"]]
    [:p "Most lawyers charge around $2000 just to get started. Expunct charges just $200."]
    [:ul
     [:li "What's the catch?"]
     [:p "The court makes you pay a fee, whether you use Expunct or a lawyer. If you're eligible to have the court costs waived, we'll help you apply."]]


    [:li [:a {:name "guarantee"}] [:b "What is the Court Cost Guarantee?"]]
    [:p "If we help you file a petition for expunction, and the court rejects it, 
we will refund our charge and also cover your court costs. That's how confident we are in our work."]
    [:p "Note: if the court rejects your petition because you don't show up to your court date,
then our Guarantee doesn't apply. Cool? Cool."]

    [:li [:a {:name "attend"}] [:b "Do I need to go to court?"]]
    [:p "Yes, after we have filed the petition with the court, we will notify you of your court date."]
    [:p "After that, you must go to court for the judge to grant the petition. Don't worry, there's no lawyering involved."]
    [:p "The judge will issue an order to clear or seal your record, and we'll make sure it gets to all the right agencies."]

    [:li [:a {:name "lawyers"}] [:b "Are you my lawyers?"]]
    [:p "No, Expunct and the Expunct team are not your lawyers, and this website is not a substitute for the advice of an attorney."]

    [:li [:a {:name "friend"}] [:b "What if I know someone else who could clear their record?"]]
    [:p "If you send a friend and they clear their record with us, we'll give you $50 as a thank you. No, there's no limit to how many friends you can refer."]

    [:li [:a {:name "whoyou"}] [:b "Who are you guys?"]]
    [:p "Did you know half of all Americans will get arrested at least once in their lifetimes? It's true, "
   [:a {:href "https://www.google.com/search?q=number+of+americans+who+will+be+arrested"} "look it up."]]
    [:p "Expunct was started by two Texas lawyers who thought you shouldn't have to hire a lawyer to clear your record. We built this web app to help you do it cheaper and faster."]

]]
     [:div.col-lg-1]]
]
    [:br]
     [:div.push]]
    [footer]]))


(secretary/defroute "/FAQ" []
  (session/put! :current-page #'about-page))
