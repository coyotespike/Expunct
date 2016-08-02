(ns lawsuitninja.calculator
  (:require 
   [secretary.core :as secretary :include-macros true]
   [reagent.core :as reagent :refer [atom]]
   [reagent.session :as session]
   [lawsuitninja.pikaday :as pikaday]
   [lawsuitninja.cljs-time :as time]
   [re-frame.core :refer [register-handler
                          path
                          register-sub
                          dispatch
                          dispatch-sync
                          subscribe]]
   [lawsuitninja.components :refer [navbar tool-tipper alt-picker2 alt-picker3]])
   (:require-macros [reagent.ratom :refer [reaction]]))


;;;; This was intended to calculate overtime, as many employees are underpaid.
;;;; The complexities of federal law regarding working periods qualifying as
;;;; overtime made for slow going, but it was fun.


(declare add-up eight-or-less? less-eight hours weekly-time wage daily-time more-than-eight)
(defn small-text-box [atomist]
    (fn []
      [:input.form-control {:type "text"
               :size 10
               :placeholder @atomist
               :on-change #(reset! atomist (-> % .-target .-value))}]))

(defn order-map [mappy]
  (apply sorted-map-by > mappy))

(defn cali-overtime-calculator 
  "Prevent overcounting. If more than 8 hours in a day, multiply by 1.5.
  If more than 12, multiply by 2. If more than 40 in a week, multiply by 1.5.
  If 7th day in a row and 8 hours or less, multiply by 1.5.
  If 7th day in a row and more than 8 hours, multiply by 2."

[wage daily-time weekly-time seventh-day-time days-per-week]
  (cond
    (> daily-time 12) (* (- daily-time 12) wage 2)
    (> daily-time 8) (* (- daily-time 8) wage 1.5)
    (> weekly-time 40) (* (- weekly-time 40) wage 1.5)
    (and 
     (> days-per-week 6) (<= daily-time 8) 
     (* seventh-day-time wage 1.5))
    (and 
     (> days-per-week 6) (> daily-time 8) 
     (* (- seventh-day-time 8) wage 1.5))))

(defn seven-day-streaks [dates-hours]
  (if (every? eight-or-less? dates-hours)
    (* 1.5 (add-up hours))
    (+ (* 1.5 (add-up (less-eight hours))) (* 2 (add-up (more-than-eight hours))))))


(defn non-streaks 
  "Accepts consecutive working periods less than 7 days. "
  [mymap]
  (let [wage 5
        hours (vals mymap)
        less-than-12 (filter (fn [x] (> 12 x)) hours)
        greater-than-12 (filter (fn [x] (<= 12 x)) hours)
        weekly-time (apply + less-than-12)
        double-time (map (fn [x] (- 12 x)) greater-than-12)
        doubler (* 2 (apply + double-time))
        long-days-total (+ (* wage 8) (* 1.5 wage 4) (* 2 double-time))]
    (+ doubler weekly-time)))

    ;; (cond
    ;;   (> weekly-time 40) (* (- weekly-time 40) wage 1.5)
    ;;   (> daily-time 8) (* (- daily-time 8) wage 1.5))))

;; An ordered map of: {date {:hours hours :wages wages :total toal}}

(defn make-totals [hours-ex]
  (loop [new []
         hours hours-ex] 
    (if (some? (first hours))
        (recur (conj new (+ (last new) (first hours)))
               (rest hours))
      new)))

(defn indices [pred coll]
   (keep-indexed #(when (pred %2) %1) coll))

(defn find-40-day-index [hour-seq]
  (first (indices (partial < 40) (make-totals hour-seq))))

;; (defn sum-all-before-day [hour-seq indie]
;;   (reduce + (subvec hour-seq 0 indie)))

(defn total-before-breakpoint-day [hours-seq]
  (nth (make-totals hours-seq) (- (find-40-day-index hours-seq) 1)))

(defn get-normal-hours-on-breakpoint-day [hours-seq]                        
  (- 40 (total-before-breakpoint-day hours-seq)))

(defn breakpoint-day 
  "Asks for hours worked on the breakpoint day, and normal hours (hours before
  the 40-hour breakpoint). Gives back correct time-and-a-half or double-time, 
  added to the normal time on that day."
  [hours normal-hours]
  (if (> hours 12) 
    (+ ( * 2 (- hours 12)) (* 1.5 (- 12 normal-hours)))
    (+ normal-hours (* 1.5 (- hours normal-hours)))))

(defn after-breakpoint-day [hours]
  (if (> hours 12) 
    (+ ( * 2 (- hours 12)) (* 1.5 12))
    (* 1.5 hours)))
    
(defn before-breakpoint-day [hours]
  (cond
    (> hours 12) (+ ( * 2 (- hours 12)) (* 1.5 4) 8)
    (> hours 8) (+ (* 1.5 (- hours 8)) 8)
    :else hours))

(defn add-week 
  "Takes sequence of hours in non-streak week, in correct order.
  Returns sum of all adjusted hours."
  [hours-seq]
  (let [break-index (find-40-day-index hours-seq)
        early-week (subvec hours-seq 0 break-index)
        breaker (nth hours-seq break-index)
        late-week (subvec hours-seq (+ 1 break-index))
        normal (get-normal-hours-on-breakpoint-day hours-seq)]
    (+
     (reduce + (map before-breakpoint-day early-week))
     (breakpoint-day breaker normal)
     (reduce + (map after-breakpoint-day late-week)))))

(defn twelve-hour-days 
  "Given a map of {date [hours wages]}, this will multiply the 
  [dates-hours] by 1.5 for the hours between 8 and 12 hours, and by 2 for
  the hours above 12."
  [dates-hours]
  (+ (* 1.5 4) (* 2 (- hours 12))))
    
(defn find-weeks
  "Given a map of {js/Date. hours} instances, divides the map up where it finds 
  a nil. Then, returns only the groups more than 7 days long.
  This implies the map must be ordered."
  [coll]
  (let [divided-up (partition-by #(nil? (val %)) coll)]
    (filter some?
  (for [streak divided-up]
    (if (<= 7 (count streak))
      streak)))))

(defn simple-daily-calculator
  "Asks for hourly rate, and hours worked more per day."
  [wage days extra-hours]
  (* @wage @days @extra-hours 1.5))

(defn calc-button [t w d h]
  (fn []
    [:input {:type "button"
             :class "btn btn-primary"
             :value "How much?" 
             :on-click #(reset! t (simple-daily-calculator w d h))}]))

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

(defn partition-between 
  "Splits coll into a lazy sequence of lists, with partition 
   boundaries between items where (f item1 item2) is true.
   (partition-between = '(1 2 2 3 4 4 4 5)) =>
   ((1 2) (2 3 4) (4) (4 5))"
  [f coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (let [fst (first s)]
        (if-let [rest-seq (next s)]
          (if (f fst (first rest-seq))
            (cons (list fst) (partition-between f rest-seq))
            (let [rest-part (partition-between f rest-seq)]
              (cons (cons fst (first rest-part)) (rest rest-part))))
          (list (list fst)))))))

(defn gap? 
  "Predicate: returns true if days are not sequential"
  [date1 date2] 
  (not= 1 (days-between date1 date2)))

(defn continuous-periods 
  "Groups a sequence of dates into continuous periods."
  [coll]
  (partition-between gap? coll))

(defn feeder
  "Takes a map which has dates as keys. Sorts the dates and then passes them
  to continuous-periods."
  [mapp]
  (continuous-periods (sort (keys mapp))))

(defn dispatch-periods [chopped]
  (for [piece chopped] 
    (let [tally (count piece)]
      (cond 
        (< 6 tally) "big" 
        (<= 2 tally) (add-week piece)
        (= 1 tally) (before-breakpoint-day piece)
;; Should throw an exception here.
        :else "crap"
))))

(defn dates-between
  "Return the dates between the two js/Date instances."
  [start-date end-date]
  (when (every? date? [start-date end-date])
    (for [day (range (days-between start-date end-date))]
      (before start-date day))))

(defn continuous? 
  "Given a sequence of dates, answers whether the dates are continuous. 
  Returns false for [Monday Wednesday], true for [Monday Tuesday]"
  [coll]
  (map #(> 2 (days-between %1 %2)) coll (rest coll)))

(defn pair-dates 
  "Match up a sequence of dates with a given value, and put it in a map."
  [dates value]
  (into {}
  (for [day dates]
    [day value])))

;; With all those exact dates and hours on it, your demand letter is going to look awesome. Not even lying.

(defn save-button [total-wages day hours] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Save"
           :on-click #(swap! total-wages assoc @day (js/parseInt @hours))}]))


(defn save-button2 [total-wages start-date end-date hours] 
  (fn []
  [:input {:type "button" 
           :class "btn btn-info" 
           :value "Save"
           :on-click #(swap! total-wages merge 
                       (pair-dates (dates-between @start-date @end-date) (js/parseInt @hours)))}]))

(def today (js/Date.))

(defn calculator-page []
  (let [wages (atom "Wages")
        days (atom "Days")
        hours (atom "Hours")
        total (atom 0)
        to-day (atom today)
        day (atom today)
        start-date (atom today)
        end-date (atom today)
        total-wages (atom {})]
    (fn []  
      [:div
       [:div.page-header
        [navbar]]
       [:div.centered-tabs
        [:ul {:class "nav nav-tabs" :role "tablist"}
         [:li {:role "presentation" :class "active"} [:a {:href "#home" :aria-controls "home" :role "tab" :data-toggle "tab"} "Single Date"]]
         [:li {:role "presentation"} [:a {:href "#profile" :aria-controls "profile" :role "tab" :data-toggle "tab"} "Lots of Days"]]]

        [:div {:class "tab-content"}

         [:div {:role "tabpanel" :class "tab-pane active fade in" :id "home"}
          [:div.parent-container
           [:div.child-column
          [alt-picker2 day to-day]]]
           [:div.parent-container
            [:div.child-column 
             [small-text-box hours]
             [:br]
             [small-text-box wages]]]
             [save-button total-wages day hours]]
    
         [:div {:role "tabpanel" :class "tab-pane fade" :id "profile"}
          [alt-picker3 start-date start-date end-date]
          [:div.parent-container
           [:div.child-column 
            [small-text-box hours]
            [:br]
           [small-text-box wages]]]

            [save-button2 total-wages start-date end-date hours]]]]

       [:div.form-centered
        [:h6 "A Few Tips"]]
       [:pre (pr @total-wages)]
         [:ul
          [:li "Choose the date, put how many hours you worked and your hourly wage, hit save. Bam."]

          [:li "It'll assume your wage stays the same, unless you change it."]
          [:li "Use Lots of Days to choose entire weeks or whatever. You'll put in your start and end date, and how many hours you worked for that entire time."]
          [:li "After you put in a day or many days, make sure to hit Save before you put in anymore."]]])))

  (defn overtime-interview []
    [:div
     [:div.page-header
      [navbar]]

     [:div
      [:div.eighty-char
       [:p "Okay, here's how we're gonna do this. Three easy steps."]
       [:ul 
        [:li "Numero the first. Grab any documents, like pay checks or W-2s, that can help you remember."]
        [:li "Numero the dos. Anytime you remember working more than 8 hours without getting paid time and a half, pick that date and write down how many hours you worked."]
        [:li "And numero the tres, anytime you remember working 7 days or more without a day off, and you didn't get paid overtime, pick those dates, and put how many hours you worked."
         [:br] "(So if you worked for two weeks straight, for an 8-hour shift, write it down for those two weeks.)"]]
       [:br]
       [:p "And you'll be done! Make sure you have already registered or logged in, or we can't save your information."]]

      [:div.form-centered
       [:a {:href "#/calculator-page" :type "button" 
            :class "btn btn-info"} "Let's Do It"]]]])
        


(secretary/defroute "/calculator-page" []
  (session/put! :current-page #'calculator-page))

(secretary/defroute "/overtime-interview" []
  (session/put! :current-page #'overtime-interview))
