(ns hindenbug.util.json)

(defn generate-string
  "Turn a clojurescript value into a JSON one."
  [val]
  (-> val clj->js js/JSON.stringify))

(defn parse
  "Parse a JSON value into a clojurescript one."
  [string]
  (-> string js/JSON.parse (js->clj :keywordize-keys false))) ; TODO change to true
