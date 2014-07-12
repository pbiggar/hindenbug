(ns hindenbug.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [hindenbug.utils :refer [mlog]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender dashboard [data owner]
  (html
   (if-let [current-team (-> data :current-team)]
     [:h1 (-> data :current-team)]
     [:h1 "Teams"])))

(defrender login-screen []
  (html
   [:div
    [:a {:href "/login"} "Login"]
    [:a {:href "/logout"} "Logout"]]))

(defrender blank-screen []
  (html
   [:div "empty"]))

(defn dominant-component [data owner]
  (print "New dominant component: " (get-in data [:navigation-point]))
  (condp = (get-in data [:navigation-point])
    :login-screen login-screen
    :dashboard dashboard
    nil blank-screen))

(defrender app [data owner]
  (om/build
   (dominant-component data owner)
   data))