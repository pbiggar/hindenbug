(ns hindenbug.util.json)

(defn generate-string
  "Turn a clojurescript value into a JSON one."
  [val]
  (.stringify js/JSON (clj->js val)))

(defn parse
  "Parse a JSON value into a clojurescript one."
  [string]
  (js->clj (.parse js/JSON string)))
