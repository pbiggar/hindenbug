(ns hindenbug.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [hindenbug.utils :refer [mlog]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender column [data]
  [:h2 "Column"])

(defrender issue-outline [data]
  [:h2 "outline"])

(defrender dashboard [data owner]
  (html
   [:div
    [:h2 "dashboard"]
    [:div
     (issue-outline (:issue data))
     (map column (:columns data))]]))

(defrender login-screen [data owner]
  (html [:div#login [:a {:href "/login"} "Login"]]))

(defrender create-issue [data owner]
  (html [:div]))

(defrender blank-screen [data owner]
  (html [:div "empty"]))

(defrender header [data owner]
  (html [:div#logout [:a {:href "/logout"} "Logout"]]))

(defn dominant-component [data]
  (print "New dominant component: " (get-in data [:navigation-point]))
  (condp = (get-in data [:navigation-point])
    :teams-overview teams-overview
    :dashboard dashboard
    :create-issue create-issue
    nil blank-screen))

(defrender inner [data owner]
  (html
   [:div#inner
    (om/build header data)
    (om/build (dominant-component data) data)]))

(defrender app [data owner]
  (html
   (if (= :login-screen (get-in data [:navigation-point]))
     (om/build login-screen data)
     (om/build inner data))))
