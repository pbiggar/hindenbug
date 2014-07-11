(ns hindenbug.login
  (:require [cemerick.url :as url]
            [goog.net.cookies :as cookie]))

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
    (when code
      (store-code code)
      (print "clearing query params")
      (clear-query-params)))

  (if (logged-in?)
    (js/alert "logged in")
    (js/alert "logged-out")))
