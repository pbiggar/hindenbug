(ns hindenbug.components.dashboard
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender column [data owner]
  (html
   [:h2 "Column"]))

(defrender issue-outline [data owner]
  (let []
    (html
     [:h2 (str "issue: " )])))


(defrender dashboard [data owner]
  (html
   [:div
    [:h2 "dashboard"]
    [:div
     (om/build issue-outline data)
     (map column (:columns data))]]))