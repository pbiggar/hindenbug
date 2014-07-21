(ns hindenbug.github
  (:require [hindenbug.tent.core :as tent :include-macros true]
            [cljs.core.async :refer (take! put! <!)]
            [hindenbug.tent.issues :as issues]
            [hindenbug.tent.search :as search]
            [hindenbug.login :as login])
  (:require-macros [hindenbug.utils :refer (inspect)]))


(defn callback [response context]
  (let [{:keys [success?]} response
        {:keys [event channel]} context]
    (put! channel [event (if success? :success :failed) {:response response
                                                         :context context}])))

(defn c [context]
  {:callback callback
   :calling-context context
   :oauth-token (login/oauth-token)})

(defn issue [number & {:as context}]
  (issues/specific-issue "circleci" "hindenbug-manual-test" number (c context)))

(defn issue-search [term & {:as context}]
  nil)