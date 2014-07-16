(ns hindenbug.components.dashboard
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defn issue [data number]
  (-> data :gh-cache inspect :issues (get number)))

(defn current-issue [data]
  (->> data :navigation-data (issue data)))

(defrender column [data owner]
  (html
   [:h2 "Column"]))

(defrender issue-outline [data owner]
  (let [issue (current-issue data)
        {:keys [number body comments state labels]} issue]
    (html
     [:h2 (str "issue: " number body comments state)])))


(defrender dashboard [data owner]
  (html
   [:div
    [:h2 "dashboard"]
    [:div
     (om/build issue-outline data)
;     (map column (:columns data))
     ]]))