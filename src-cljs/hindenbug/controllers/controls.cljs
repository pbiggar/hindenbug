(ns hindenbug.controllers.controls
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [hindenbug.utils :as utils :include-macros true]
            [hindenbug.util.common-words :as common]
            [clojure.string :as str])
  (:require-macros [dommy.macros :refer [sel sel1]]
                   [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                   [hindenbug.utils :refer (inspect)])
  (:import [goog.fx.dom.Scroll]))

;; --- Helper Methods ---

(defn container-id [container]
  (int (last (re-find #"container_(\d+)" (.-id container)))))

;; --- Navigation Multimethod Declarations ---

(defmulti control-event
  ;; target is the DOM node at the top level for the app
  ;; message is the dispatch method (1st arg in the channel vector)
  ;; state is current state of the app
  ;; return value is the new state
  (fn [target message args state] state))

(defmulti post-control-event!
  (fn [target message args previous-state current-state] message))

;; --- Navigation Multimethod Implementations ---

(defmethod control-event :default
  [target message args state]
  (utils/mlog "Unknown controls: " message)
  state)

(defmethod post-control-event! :default
  [target message args previous-state current-state]
  (utils/mlog "No post-control for: " message))


(defmethod control-event :search-key-up
  [target message args state]
  (assoc-in state [:search :title] (:value args)))

(defn search-last-word [string]
  (when-let [last-word (-> string (str/split #" ") last)]
    (when (-> last-word common/words not)
      (print last-word))))

(defmethod post-control-event! :search-key-up
  [target message args previous-state current-state]
  (when (#{13 32} (:key args))
    (search-last-word (:value args))))

(defmethod post-control-event! :search-blur
  [target message args previous-state current-state]
  (search-last-word (:value args)))