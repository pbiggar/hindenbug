(ns hindenbug.login
  (:require [cemerick.url :as url]
            [hindenbug.util.json :as json]
            [goog.net.cookies :as cookie])
  (:require-macros [hindenbug.utils :refer [inspect]]))

(defn oauth-token []
  (some-> "hindenbug-session"
          cookie/get
          js/decodeURIComponent
          json/parse
          (get "token")))


(defn logged-in? []
  (-> (oauth-token) nil? not))
