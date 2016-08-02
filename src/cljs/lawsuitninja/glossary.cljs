(ns lawsuitninja.glossary
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [goog.dom :as dom]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [lawsuitninja.components :refer [navbar typeahead beeminder-graph]])
  (:import goog.History))


(defn glossary-page []
  [:div
   [:div.page-header
    [navbar]]

   [:div.form-centered
    [:h3 "The Expunct Blog"]
    [:h5 [:i "Recent Posts"]]]
   [:div.container
    [:div.row
     [:div.col-lg-8.col-lg-offset-2.col-md-10.col-md-offset-1
      [:div.post-preview 
       [:a {:href "#/mysterious"}
        [:h6.post-title "Conviction, and other mysterious terms"]
        [:h6.post-subtitle "Texas expunction law is way too complicated."]]
       [:p.post-meta "Posted by Expunct on October 20, 2015"]]]]]])


(secretary/defroute "/glossary" []
  (session/put! :current-page #'glossary-page))

(defn mysterious-terms []
  [:div
   [:div.page-header
    [navbar]]

   [:div.form-centered 
    [:h6 "Conviction, and other mysterious terms"]
    [:p "The terms used for this area of law are confusing and overlapping."]
    [:p "Here is a glossary to help explain what they mean, and some of the key differences between them."]
    [:p "All of these definitions apply in Texas, which is where we are focused right now."]]
   [:ol
    [:a {:href "#Disposition"} [:li "Disposition"]]
    [:a {:href "#Conviction"} [:li "Conviction"]]
    [:a {:href "#Dismissal"} [:li "Dismissal"]]
    [:a {:href "#Straight Dismissal"} [:li "Straight Dismissal"]]
    [:a {:href "#Probation"} [:li "Probation"]]
    [:a {:href "#Deferred Adjudication"} [:li "Deferred Adjudication"]]
    [:a {:href "#Community Supervision (Straight Probation)"} [:li "Community Supervision (Straight Probation)"]]
    [:a {:href "#Deferred Prosecution Dismissal"} [:li "Deferred Prosecution Dismissal"]]
    [:a {:href "#Deferred Disposition Dismissal"} [:li "Deferred Disposition Dismissal"]]
    [:a {:href "#Pretrial Diversion"} [:li "Pretrial Diversion"]]
    [:a {:href "#Private Background Check Companies"} [:li "Private Background Check Companies"]]]

   [:ol.eighty-char
    [:li [:a {:name "Disposition"}] [:b "Disposition"]]
    [:p "The disposition is the name for the way the case ended (was disposed of)."]

    [:li [:a {:name "Conviction"}] [:b "Conviction"]]
    [:p "A Conviction indicates that a court has found you guilty of some offense. You really don’t want to end up with a one on your record, because these cannot be expunged or sealed, except for Class C Misdemeanors."]

    [:li [:a {:name "Dismissal"}] [:b "Dismissal"]]
    [:p "A Dismissal indicates that you were not found guilty of the offense you were charged with. Awesome! I can move forward with my life! 

Well, not quite. You might think that the police, prosecutor and courts would shred your files and delete your records, never to surface again. In fact, this is what most people imagine happens when the case is dismissed. In reality, you still have records of you arrest and court appearances on file. These records are sold to background check companies, and your future employers can look them up. 

Until you take the steps to apply for an expunction, these records can impact your job search or your application for a professional license."]


    [:li [:a {:name "Straight Dismissal"}] [:b "Straight Dismissal"]]
    [:p "A straight dismissal is where the prosecutor dismisses the case. Usually the prosecutor will dismiss if, on reviewing the evidence, it becomes clear that the charge cannot be supported - or the interests of justice would be best served by not bringing the case to trial."]
    [:p "For example, I had a friend who was driving to church early on a Sunday morning when the roads were empty. He was pulled over by a cop who cited him for driving the wrong way up a one-way street. He explained that he lived just around the corner, drove up this street every day to work, and that he knew for sure that he wasn’t traveling in the wrong direction. The cop refused to believe this, so my friend went to his hearing and met with the prosecutor. Once he explained the mix-up, the prosecutor laughed and said that she drove up the same street to work each day too, so she knew full well that the cop had been mistaken. The prosecutor immediately dismissed the case: there wasn’t the evidence to convict my friend."]
    [:p "This is the best case scenario, because a straight dismissal is definitely eligible for expunction."]

    [:li [:a {:name "Probation"}] [:b "Probation"]]
    [:p "We all know this one, right? It’s the one where you can stay out of jail so long as stay our of trouble for the probationary period! Well nearly. Everyone uses the word probation, but in Texas probation is called Community Supervision, technically."]
    [:p "The gist of Community Supervision is that you can avoid jail for the offense you were charged with, if for the specified period of time, you follow all the requirements as ordered by the judge - especially not getting any other offenses. If you breach the requirements, the D.A. can ask the judge to revoke your probation and you’ll be put in jail."] 
    [:p "There are two types: (1) regular Community Supervision (you might know it as Straight Probation) and (2) Deferred Adjudication."] 
    [:p "These two types are confusing because many defendants think that Deferred Adjudication, if successfully completed, will vanish from your record. But this is not the case: it cannot be Expunged, but may be Ordered Non-Disclosed."]
    [:p "A Deferred Adjudication successfully completed means that you’ll (1) stay out of jail and (2) avoid a Conviction and (3) may be able to have the records sealed (ordered Non-Disclosed)."] 
    [:p "A Community Supervision successfully completed means (1) you’ll stay out of jail, but (2) you will have a Conviction and (3) you cannot get it Non-Disclosed."]

    [:li [:a {:name "Deferred Adjudication"}] [:b "Deferred Adjudication"]]
    [:p "Deferred Adjudication is a type of Probation (Community Supervision). Usually this is offered only to first-time offenders. You can think of Deferred Adjudication as half way between Deferred Prosecution (expungeable record) and Community Supervision (permanent record)."]

    [:p "Technically, the case is Dismissed and you didn’t get a Conviction. However, you cannot get it expunged, you can only get it sealed. If the offense is sealed, it would be sealed from a regular employment background check. But state agencies will still have a copy of the record."]

    [:p "Confusingly, at the federal level, a Deferred Adjudication record is treated as a Conviction. This impacts your immigration status and gun rights."]

    [:li [:a {:name "Community Supervision (Straight Probation)"}] [:b "Community Supervision (Straight Probation)"]]
    [:p "If you complete your regular Community Supervision successfully, you have a Conviction, which cannot be expunged or sealed. This is the way that most regular Community Supervision sentences come to an end.

Your lawyer can apply to have you Community Supervision “set aside” after completing 1/3 or 2 years of your sentence. The benefit is that your case will be “dismissed” by the judge, and you will not end up with a final Conviction on your record. However, you still cannot have this expunged or sealed and it will still impact a job search."

    [:li [:a {:name "Deferred Prosecution Dismissal"}] [:b "Deferred Prosecution Dismissal"]]
     [:p "This terminology is something you’ll see in Travis County. It’s a type of dismissal. If you were charged with a felony, you can get this only if you are a first-time offender and this was a non-violent crime.  

Usually, you’ll be offered it by the prosecutor as part of a deal/plea bargain if you have an otherwise-clean criminal record. In exchange for the prosecutor dismissing the case, there will be a period of time in which you must not commit other crimes, and may have to do community service, or attend counseling. If you do this successfully, the prosecutor will not re-file charges and your case will remain dismissed. 

The good news is, once you’ve completed the requirements successfully, you can have this dismissal expunged from your record."]

    [:li [:a {:name "Deferred Disposition Dismissal"}] [:b "Deferred Disposition Dismissal"]]
     [:p "This is similar to a Deferred Prosecution Dismissal, but for Class C Misdemeanors. This can also be expunged once completed."]
    [:li [:a {:name "Pretrial Diversion"}] [:b "Pretrial Diversion"]]
     [:p "Pretrial Diversion is a program that may be offered where the offense is low-level and non-violent. This too can be expunged once completed."
    [:li [:a {:name "Private Background Check Companies"}] [:b "Private Background Check Companies"]]

      [:p "Can we just agree right now that all this is a little confusing? There are many ways for a case to end and each disposition has a different effect. Also, the terminology is baffling - why do two things that sound almost interchangeable (Deferred Adjudication and Deferred Prosecution) have such different effects on your ability to clear your record?"]

      [:p "Despite the murkiness in this area, two things have happened recently. One, employers are ever more eager to run background checks on employees and two, counties and the state sell their court databases to multiple private background check companies. 

Given the complexity, you won’t be surprised to hear that a background check companies don’t always correctly report things to employers.
For instance, a background check company might report you got a Conviction, when you actually got a Deferred Adjudication or a Community Supervision, and this might disqualify you from a job or promotion."]

      [:p "What is worse, if you try to search an online county database to check your own criminal record, many times the search engine will return a false negative. The database maintained by many of the counties are incomplete in our experience. This means that, even if there is a record there, your search won’t return it - giving you false sense of security. However, your potential employer, who is paying a background check company, will certainly find whatever records are in the system."]

      [:p "For this reason, it’s worth giving yourself the peace of mind that comes from a running a proper background check on yourself before you apply for jobs or professional licenses. If there’s nothing there, great - you can be confident in your applications. If something does show up, an Expunction Order or Order of Nondisclosure can clear or seal your record."]]]]
   [:br]
   [:a.form-centered {:href "#/glossary"} [:p "Back to the Blog"]]
])
(secretary/defroute "/mysterious" []
  (session/put! :current-page #'mysterious-terms))
