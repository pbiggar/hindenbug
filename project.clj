(defproject hindenbug "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [com.cemerick/url "0.1.1"]
                 [secretary "1.2.1-SNAPSHOT"]
                 [om "0.6.4"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [cljs-http "0.1.14"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "hindenbug"
              :source-paths ["src"]
              :compiler {
                :output-to "hindenbug.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
