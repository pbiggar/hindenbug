(ns hindenbug.util.json)

(defn parse
  "Parse a JSON value into a clojurescript one."
  [string]
  (js->clj (.parse js/JSON string)))
