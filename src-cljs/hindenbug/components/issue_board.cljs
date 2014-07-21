(ns hindenbug.components.issue-board
  (:require [om.core :as om :include-macros true]
            [clojure.string :as str]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))


(defn issue [data number]
  (-> data :gh-cache :issues (get number)))

(defn current-issue [data]
  (->> data :navigation-data (issue data)))

(defn label->html [{:as label :keys [url name color]}]
  [:div {:background-color color}
   [:a {:href url} name]])

(defn user->html [{:as user :keys [html_url login avatar_url]}]
  [:div
   [:img {:src avatar_url :width 20 :height 20}]
   [:a {:href html_url} login]])

(defrender columns [data owner]
  (html
   [:h2 "Columns"]))

(defrender issue-outline [data owner]
  (let [issue (current-issue data)
        {:keys [user closed_by number body title comments state labels]} (inspect issue)]
    (html
     (if-not issue
       [:div "loading"]
       [:div
        [:h2 (str "issue: " number)]
        [:div "opened by " (-> user user->html html)]
        (when closed_by
          [:div "closed by " (-> closed_by user->html html)])
        [:div "log: " title]
        [:div "state: " state]
        [:div "num-comments: " comments]
        [:div "labels: " (map label->html labels)]]))))


(defrender issue-board [data owner]
  (html
   [:div
    [:h2 "dashboard"]
    [:div
     (om/build issue-outline data)
     (om/build columns data)]]))