(defproject lawsuitninja "0.1.0-SNAPSHOT"
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308" :scope "provided"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-server "0.4.0"]
                 [cljsjs/react "0.13.3-0"]
                 [reagent-forms "0.5.4"]
                 [reagent-utils "0.1.5"]
                 [prone "0.8.2"]
                 [compojure "1.3.4"]
                 [hiccup "1.0.5"]
                 [environ "1.0.0"]
                 [secretary "1.2.3"]

                 [cljs-http "0.1.35" :exclusions [commons-codec]]
                 [clj-http "2.0.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring-curl "0.3.0"]
                 [com.taoensso/carmine "2.10.0" :exclusions [commons-codec]]
                 [noencore "0.1.21" :exclusions [commons-codec]]
                 [com.velisco/tagged "0.3.0"]
                 [cljs-pikaday "0.1.2"]
                 [lein-try "0.4.3"]
                 [prismatic/schema "0.4.3"]
                 [prismatic/plumbing "0.4.1"]
                 [spellhouse/clairvoyant "0.0-72-g15e1e44"]
                 [ring-transit "0.1.3" :exclusions [prismatic/plumbing]]
                 [com.draines/postal "1.11.3"]
                 [clj-time "0.9.0"]
                 [com.andrewmcveigh/cljs-time "0.3.11"]
                 [com.cemerick/url "0.1.1"]
                 [clj-pdf "2.0.8" :exclusions [com.itextpdf/itextpdf]]
                 [camel-snake-kebab "0.3.1" :exclusions [org.clojure/clojure]]
                 [cljsjs/stripe "2.0-0"]
                 [reagent "0.5.1"]
                 [re-frame "0.4.0"]
                 [re-com "0.6.2"]
                 [enlive "1.1.5"]
                 [prismatic/dommy "1.1.0"]
                 [enfocus "2.1.1"]
                 [org.clojars.frozenlock/reagent-modals "0.2.3"]
                 [org.ccil.cowan.tagsoup/tagsoup "1.2.1"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [alandipert/storage-atom "1.2.4"]
                 [com.novemberain/validateur "2.4.2"]
                 [cljsjs/bootstrap "3.3.5-0"]
                 [abengoa/clj-stripe "1.0.4"]
                 [sitemap "0.2.4"]
                 [venantius/accountant "0.1.1"]]


  :plugins [[lein-environ "1.0.0"]
            [lein-asset-minifier "0.2.2"]
            [lein-auto "0.1.2"]
            [lein-cljsbuild "1.0.6"]
            [com.cemerick/clojurescript.test "0.3.2"]]


  :resource-paths ["resources"
                   "resources/public"
                   "resources/public/css"
                   "resources/public/css/vendor"
                   "resources/public/fonts/glyphicons"
                   "resources/public/fonts/lato"
                   "resources/public/img"
                   "resources/public/icons/png"
                   "resources/public/icons/svg"
                   "resources/public/login"
                   "resources/public/tile"
                   "resources/public/js"
                   "resources/templates"]

  :ring {:handler lawsuitninja.handler/app
         :uberwar-name "lawsuitninja.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "lawsuitninja.jar"

  :main lawsuitninja.server
  :hooks [leiningen.cljsbuild]
  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :minify-assets
  {:assets
    {"resources/public/css/flat-ui-pro.css" 
     "resources/public/css/flat-ui-pro.min.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns lawsuitninja.repl
                                  :nrepl-middleware []
                                  ;; defaults to 30000, 30 seconds
                                  :timeout 120000}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [leiningen-core "2.5.1" :exclusions [org.apache.maven.wagon/wagon-provider-api]]
                                  [lein-figwheel "0.3.5" :exclusions 
                                   [org.clojure/clojure]
                                   [org.clojure/clojurescript]
                                   [com.google.javascript/closure-compiler]
                                   [org.clojure/data.json]
                                   [org.mozilla/rhino]
                                   [com.google.javascript/closure-compiler-externs]
                                   [org.codehaus.plexus/plexus-utils]
                                   [clj-stacktrace]
                                   [com.cemerick/piggieback]]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.3.3"]
                             [lein-cljsbuild "1.0.6"]
                             [com.cemerick/clojurescript.test "0.3.2"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7002
                              :css-dirs ["resources/public/css"]
                              :ring-handler lawsuitninja.handler/app}

                   :env {:dev true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "lawsuitninja.dev"
                                                         :source-map true}}
                                        :test {:source-paths ["src/cljs"  "test/cljs"]
                                               :compiler {:output-to "target/test.js"
                                                          :optimizations :whitespace
                                                          ; uncommented this Oct 6
                                                          ;; :source-map true
                                                          ;; :pretty-print true
}}}
                               :test-commands {"unit" ["phantomjs" :runner
                                                       "test/vendor/es5-shim.js"
                                                       "test/vendor/es5-sham.js"
                                                       "test/vendor/console-polyfill.js"
                                                       "target/test.js"]}}}

             ;;;; commented out minify-assets as it fucked with my css
             :uberjar {:hooks [leiningen.cljsbuild]; minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:output-to "resources/public/js/app.js"
                                               :optimizations :advanced
                                               :externs ^:replace [
                                                                   "externs/jquery.1.9.js" 
                                                                   ;; "externs/google-maps-api.js" "externs/google_maps_api_v3_exp.js"
                                                                   "externs/my-externs.js"
                                                                   "externs/twitter-bootstrap.js" 
;                                                                   "resources/public/js/flat-ui-pro.min.js"
                                                                   ;; "externs/bootstrap.min.inc.js"
]
                                               :pretty-print false}}}}}})
