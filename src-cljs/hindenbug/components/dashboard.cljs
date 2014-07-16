(ns hindenbug.components.dashboard
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defn issue [data number]
  (-> data :gh-cache :issues (get number)))

(defn current-issue [data]
  (->> data :navigation-data (issue data)))

(defn label->html [{:as label :keys [url name color]}]
  [:div {:background-color color}
   [:a {:href url} name]])

(defrender column [data owner]
  (html
   [:h2 "Column"]))

(defrender issue-outline [data owner]
  (let [issue (current-issue data)
        {:keys [number body title comments state labels]} (inspect issue)]
    (html
     [:div
      [:h2 (str "issue: " number)]
      [:div "log: " title]
      [:div "state: " state]
      [:div "num-comments: " comments]
      [:div "labels: " (map label->html labels)]])))


(defrender dashboard [data owner]
  (html
   [:div
    [:h2 "dashboard"]
    [:div
     (om/build issue-outline data)
;     (map column (:columns data))
     ]]))