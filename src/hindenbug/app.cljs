(ns hindenbug.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [sablono.core :as html])
  (:require-macros [hindenbug.utils :refer (inspect)]))

(defn dashboard [data owner]
  (reify
   om/IRender
   (render [this]
     (if-let [current-team (-> data :current-team)]
       (dom/h1 nil (-> data :current-team))
       (dom/h1 nil "Teams")))))

(defn login-screen [data owner]
  (reify
   om/IRender
   (render [_]
     (dom/a
      {:href "https://github.com/login/oauth/authorize?client_id=48e1586e94ff6d0fe5a4&scope=repo"}
      "Login"))))

(defn dominant-component [app-state]
  (print "New dominant component: " (get-in app-state [:navigation-point]))
  (condp = (get-in app-state [:navigation-point])
    :login-screen login-screen
    :dashboard dashboard
    nil (dom/div nil "")))

(defn app [app-state owner]
  (reify
    om/IDisplayName (display-name [_] "App")
    om/IRender
    (render [_]
      (dom/div nil (dominant-component app-state)))))