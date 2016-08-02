(ns lawsuitninja.formtemplate 
  (:require
   [ring.middleware.keyword-params :refer [wrap-keyword-params keyword-params-request]]
   [lawsuitninja.redis :refer [update-Redis read-from-Redis]])
  (:use clj-pdf.core
        ring.util.io))





(defn return-pdf 
  "I was unable to figure out how to dynamically redefine the vars.
  Therefore, I have wrapped them all in this function.
  The function evaluates the request, and takes out user data.
  It then plugs the user data into var vectors/maps, and then applies
  the templates to those vars.
  The function returns the completed pdf.
  Yes, it is a bad idea."
  [req]
  (let [request (keyword-params-request req)
        username (get-in request [:query-params "username"])
        BigAtom (:BigAtom (read-from-Redis username))
        data BigAtom

 plaintiff  [{:plaintiff-name (:yourname data)
                 :precinct (:empty-precinct data)
                 :place (:empty-place data)
                 :defendant-name (:landlordname data)
                 :county (:your-county data)}]

chapter1 [{:section-heading "DISCOVERY" :number "I. "}]
chapter2 [{:section-heading "PARTIES" :number "II. "}]
chapter3 [{:section-heading "JURISDICTION AND VENUE" :number "III. "}]
chapter4 [{:section-heading "FACTS" :number "IV. "}]
chapter5 [{:section-heading "CAUSE OF ACTION" :number "V. "}]
chapter6 [{:section-heading "REQUEST FOR RELIEF" :number "VI. "}]


content1 [{:content "Plaintiff intends to conduct discovery under Level 1."
                :number "1. "}]

content2 [{:content ", Plaintiff, is a resident of the county of this Court, within Texas."
                :name (:yourname data)
                :number "2. "}]
content3 [{:content ", Defendant, may be served at the following address: " 
               :name (:landlordname data)
                :number "3. "
               :address (:landlord-address data)}]

content4 [{:content "The amount in controversy is within the jurisdictional limits of of this Court, and venue is proper as the cause of action arose in the county of this Court within Texas."
                :number "4. "}]

content5 [{:content (:the-facts data)
           :number "5. "}]

content6 [{:content "Defendant is liable to Plaintiff for failing to return the security deposit. Pursuant to Section 92.109 of the Texas Property Code, Plaintiff is entitled to $100; three times the aamount of the security deposit wrongfully withheld, and court costs from Defendant."
                :number "6. "}]
content7 [{:content "Plaintiff requests this Court grant the following relief:" 
                :lister [:list {:lowercase true}
                         [:chunk "$100 civil penalty;"]
                         [:chunk "three times the amount of the security deposit wrongfully withheld;"]
                         [:chunk "court costs; and"]
                         [:chunk "any other relief to which Plaintiff is entitled."]]}]

sig-content [{:name (:yourname data)
                   :address (:your-address data)
                   :city-state-zip (:city-state data)
                   :phone "123-456-7890?"}]

heading-template

  (template
   [:paragraph {:align :center}
    [:heading "NO. _________________"]  
    [:pdf-table {:cell-border false :horizontal-align :center :width-percent 100}
     [30 30 30]

     [[:pdf-cell {:valign :middle} $plaintiff-name ]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      "IN THE JUSTICE COURT"]

     [[:pdf-cell {:valign :middle} "PLAINTIFF," ]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      ""]

     [[:pdf-cell {:valign :middle} "" ]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      [:pdf-cell (str "PRECINCT " $precinct ",")]]

     [[:pdf-cell {:valign :middle} "vs." ]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      [:pdf-cell (str "PLACE " $place ",")]]

     [[:pdf-cell {:valign :middle} "" ]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      [:pdf-cell ""]]

     [[:pdf-cell {:valign :middle} $defendant-name]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      [:pdf-cell (str $county " COUNTY, TEXAS")]]

     [[:pdf-cell {:valign :middle} "DEFENDANT." ]
      [:pdf-cell {:align :center :valign :middle} ")" ]
      ""]

     [[:pdf-cell ""]
      [:pdf-cell {:colspan 3 :align :justified :styles [:bold :underline] :padding-top 15} "PLAINTIFF'S ORIGINAL PETITION" ]
      ""]

     ["" "" ""]
     ["" "" ""]
     ]])

 salutation
  [:paragraph    
   {:indent 20}


   [:chunk "TO THE HONORABLE JUDGE OF THE COURT:"] "\n" "\n"
   [:chunk "Plaintiff files this original petition in the the above-styled and numbered cause, and in support, shows the Court as follows:"]
   [:spacer 2]
]


 section-heading 
  (template
   [:paragraph {:align :center :style :bold} $number $section-heading
   [:spacer]])


 section-content
  (template
   [:paragraph {:align :left} $number $name $content $address $lister
    [:spacer]])


 signature-block
  (template
   [:paragraph {:align :right}
    [:list {:symbol ""}
     [:chunk "Respectfully submitted, "]
     [:chunk "____________________________"]
     [:chunk $name]
     [:chunk $address]
     [:chunk $city-state-zip]
     [:chunk $phone]]])
]

    (piped-input-stream
     (fn [output-stream]

  (pdf
   [{:font {:size 12
            :family :times-roman}
     :size :letter
     :right-margin 80
     :left-margin 80
     :top-margin 20
     :bottom-margin 10}

    (heading-template plaintiff)
    salutation
    (section-heading chapter1)
    (section-content content1)
    (section-heading chapter2)
    (section-content content2)
    (section-content content3)
    (section-heading chapter3)
    (section-content content4)
    (section-heading chapter4)
    (section-content content5)
    (section-heading chapter5)
    (section-content content6)
    (section-content content6)
    (signature-block sig-content)
    ]
   output-stream)))))

(defn make-pdf 
  "Returns the completed template, with appropriate content headers."
  [req]
  {:headers {"Content-Type" "application/pdf"}
   :body (return-pdf req)})
