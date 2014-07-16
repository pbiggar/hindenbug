(ns hindenbug.components.dashboard
  (:require [om.core :as om :include-macros true]
            [clojure.string :as str]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender dashboard [data owner]
  (html
   [:div
    [:h2 "top-level dashboard - nothing to see here yet"]]))