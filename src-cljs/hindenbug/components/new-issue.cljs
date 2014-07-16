(ns hindenbug.components.new-issue
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defrender create-issue [data owner]
  (html [:div]))
