(ns hindenbug.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cemerick.url :as url]))

(enable-console-print!)

(defn widget [data owner]
  (reify
    om/IRender
    (render [this]
      (dom/h1 nil (:text data)))))

(defn respond-to-oauth-code []
  (let [code (-> js/document.URL url/url :query (get "code"))

        ;; bug in routing adds a '/' suffix
        code (.substring code 0 (- (count code) 1))]

    (print code)))

(respond-to-oauth-code)

(om/root widget {:text "Hello world!"}
  {:target (. js/document (getElementById "app"))})
