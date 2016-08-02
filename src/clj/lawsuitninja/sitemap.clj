(ns lawsuitninja.sitemap
  (:require [sitemap.core :refer [generate-sitemap]]))

(def locs ["https://www.expunct.com/#/FAQ"
           "https://www.expunct.com/#/checkout"
           "https://www.expunct.com/#/ABFelony"
           "https://www.expunct.com/#/acquittal"
           "https://www.expunct.com/#/norelations"
           "https://www.expunct.com/#/relatedcharges"
           "https://www.expunct.com/#/relatedcharges"
           "https://www.expunct.com/#/cleared"
           "https://www.expunct.com/#/not-cleared"
           "https://www.expunct.com/#/classC"
           "https://www.expunct.com/#/contact-info"
           "https://www.expunct.com/#/review"
           "https://www.expunct.com/#/notsure"
           "https://www.expunct.com/#/sealed"
           "https://www.expunct.com/#/reset-password"
           "https://www.expunct.com/#/register"
           "https://www.expunct.com/#/felonydismissed"
           "https://www.expunct.com/#/ClassABdmissed"
           "https://www.expunct.com/#/classCdismissed"
           "https://www.expunct.com/#/dismissed"
           "https://www.expunct.com/#/nonexpungeable"
           "https://www.expunct.com/#/expungeable"
           "https://www.expunct.com/#/nocontestguilty"
           "https://www.expunct.com/#/glossary"])
(def info ["2015-10-20" "daily"])
(def vecofvec (vec (map vec (map #(cons % info) locs))))
(def ks [:loc :lastmod :changefreq :priority])
(def mapofsite (vec (map #(zipmap ks %) vecofvec)))


(defn sitemap []
  (generate-sitemap mapofsite))
