(defproject hindenbug "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ankha "0.1.2"]
                 [org.clojure/clojurescript "0.0-2268"]
                 [com.google.javascript/closure-compiler "v20131014"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [cljs-ajax "0.2.6"]
                 [om "0.6.4"]
                 [com.facebook/react "0.10.0"] ;; include for externs
                 [prismatic/dommy "0.1.2"]
                 [sablono "0.2.16"]
                 [secretary "1.2.1-SNAPSHOT"]
                 [com.andrewmcveigh/cljs-time "0.1.5"]
                 [weasel "0.3.0"] ;; repl
                 [com.cemerick/url "0.1.1"]
                 ;; tests
                 [com.cemerick/clojurescript.test "0.3.0"]

                 ;; backend
                 [ring/ring "1.3.0"]
                 [hiccup "1.0.5"]]

  :plugins [[lein-cljsbuild "1.0.3"]]

  :source-paths ["src"]

  :main hindenbug.server
  :hooks [leiningen.cljsbuild]

  :uberjar-name "backend.jar"

  :min-lein-version "2.0.0"

  :cljsbuild
  {:builds
   {:dev {:id "hindenbug"
          :source-paths ["src-cljs"]
          :compiler {:output-to "hindenbug.js"
                     :output-dir "out"
                     :optimizations :none
                     :pretty-print true
                     :source-map true}}
    :prod {:id "hindenbug"
           :source-paths ["src-cljs"]
           :compiler {:output-to "resources/public/js/hindenbug.js"
                      :optimizations :advanced
                      :pretty-print false
;                      :source-map true
                      }}}})
