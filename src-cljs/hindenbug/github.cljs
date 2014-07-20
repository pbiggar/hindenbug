(ns hindenbug.github
  (:require [hindenbug.tent.issues :as issues]
            [hindenbug.tent.search :as search]))

(defn issue [number]
  ()
  (issues/issue "circleci" "hindenbug-manual-test" number)
  )