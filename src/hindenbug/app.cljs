(ns hindenbug.app
  (:require [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]))

(defn team-overview [data owner]
  (dom/h1 nil (-> data :current-team)))

(defn team-listing [data owner]
  (reify
   om/IRender
   (render [this]
     (if-let [current-team (-> data :current-team)]
       (team-overview data owner)
       (dom/h1 nil "Teams")))))

(defn app [app owner]
  (team-listing app owner))