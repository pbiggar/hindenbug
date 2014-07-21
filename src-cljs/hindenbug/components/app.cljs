(ns hindenbug.components.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [hindenbug.components.dashboard :as dashboard]
            [hindenbug.components.issue-board :as issue-board]
            [hindenbug.components.login :as login]
            [hindenbug.components.new-issue :as new-issue]
            [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [hindenbug.utils :refer [mlog]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender blank-screen [data owner]
  (html [:div "empty"]))

(defrender header [data owner]
  (html
   [:div
    [:div#logout [:a {:href "/logout"} "Logout"]]
    [:div#new [:a {:href "/issues/new"} "New issue"]]]))

(defn dominant-component [data]
  (print "New dominant component: " (get-in data [:navigation-point]))
  (condp = (get-in data [:navigation-point])
    :dashboard dashboard/dashboard
    :issue-board issue-board/issue-board
    :create-issue new-issue/new-issue
    nil blank-screen))

(defrender app [data owner]
  (html
   (if (= :login-screen (get-in data [:navigation-point]))
     (om/build login/login-screen data)
     [:div#inner
      (om/build header data)
      (om/build (dominant-component data) data)])))
