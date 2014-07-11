(defproject hindenbug "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [com.cemerick/url "0.1.1"]
                 [om "0.6.4"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :source-paths ["src"]

  :cljsbuild {
    :builds [{:id "hindenbug"
              :source-paths ["src"]
              :compiler {
                :output-to "hindenbug.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
