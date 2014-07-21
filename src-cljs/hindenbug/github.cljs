(ns hindenbug.github
  (:require [hindenbug.tent.core :as tent :include-macros true]
            [hindenbug.tent.issues :as issues]
            [hindenbug.tent.search :as search]
            [hindenbug.login :as login])
  (:require-macros [hindenbug.utils :refer (inspect)]))


(defn callback [context response]
  (js/alert "callback"))

(defn issue [number & {:as context}]
  (issues/specific-issue "circleci" "hindenbug-manual-test" number {:callback callback
                                                                    :calling-context context
                                                                    :oauth-token (inspect (login/oauth-token))}))

(defn issue-search [term & {:as context}]
  nil)