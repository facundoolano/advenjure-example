(defproject example "0.1.0-SNAPSHOT"
  :description "Example game for the advenjure engine"
  :url "https://github.com/facundoolano/advenjure-example"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljsbuild "1.1.4"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [advenjure "0.4.0"]]
  :cljsbuild
    {:builds
     {:main {:source-paths ["src"]
             :compiler {:output-to "main.js"
                        :main example.core
                        :optimizations :simple
                        :pretty-print false
                        :optimize-constants true
                        :static-fns true}}

      :dev {:source-paths ["src"]
            :compiler {:output-to "dev.js"
                       :main example.core
                       :optimizations :none
                       :source-map true
                       :pretty-print true}}}}


  :main ^:skip-aot example.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
