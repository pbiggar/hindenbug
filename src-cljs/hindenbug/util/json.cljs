(ns hindenbug.util.json
  (:require-macros [hindenbug.utils :refer [inspect]]))

(defn generate-string
  "Turn a clojurescript value into a JSON one."
  [val]
  (-> val clj->js js/JSON.stringify))

(defn parse-string
  "Parse a JSON value into a clojurescript one."
  [string & {:keys [keywordize] :or {keywordize true}}]
  (-> string js/JSON.parse (js->clj :keywordize-keys keywordize)))
