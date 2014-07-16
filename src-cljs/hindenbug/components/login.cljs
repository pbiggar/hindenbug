(ns hindenbug.components.login
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender login-screen [data owner]
  (html [:div#login [:a {:href "/login"} "Login"]]))
