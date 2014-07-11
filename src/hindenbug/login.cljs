(ns hindenbug.login
  (:require [cemerick.url :as url]))

(defn query-code []
  (let [code (-> js/document.URL url/url :query (get "code"))

        ;; bug in routing adds a '/' suffix
        code (.substring code 0 (- (count code) 1))]

    (print code)))

(defn login-cookie []
  (cookie/get :oauth))

(defn logged-in? []
  (-> (login-cookie) seq))

(defn store-code [code]
  (cookie/set :oauth code))

(defn clear-query-params []
  (print "TODO"))

(defn login []
  (let [code (query-code)]
    (print code)
    (when code
      (store-code code)
      (print "clearing query params")
      (clear-query-params)))


  (if (logged-in?)
    (js/alert "logged in")
    (js/alert "logged-out")))

(defn init []
  (let [node (om/root widget {:text "Hello world!"}
                      {:target (. js/document (getElementById "app"))})]
    (history/new-history-imp node)
    (login)))
