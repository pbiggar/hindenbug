(ns hindenbug.github
  (:require [hindenbug.tent.core :as tent :include-macros true]
            [hindenbug.tent.issues :as issues]
            [hindenbug.tent.search :as search]
            [hindenbug.login :as login]))


(defn callback [context response]
  (js/alert "callback"))

(defn issue [number & {:as context}]
  (tent/with-defaults {:callback callback
                       :calling-context context
                       :oauth_token (login/oauth-token)}
    (issues/issue "circleci" "hindenbug-manual-test" number)))

(defn issue-search [term & {:as context}]
  nil)