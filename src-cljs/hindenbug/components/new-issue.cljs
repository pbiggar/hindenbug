(ns hindenbug.components.new-issue
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender new-issue [data owner]
  (html
   [:div#create-issue
    [:h2 "Create a new issue"]
    [:div [:input#title {:type "text"}]]
    [:div [:textarea#body]]]))
