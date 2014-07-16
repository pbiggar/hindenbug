(ns hindenbug.controllers.api
  (:require [cljs.core.async :refer [close!]]
            [goog.string :as gstring]
            [hindenbug.utils :as utils :refer [merror mlog]])
  (:require-macros [hindenbug.utils :refer [inspect]]))

;; when a button is clicked, the post-controls will make the API call, and the
;; result will be pushed into the api-channel
;; the api controller will do assoc-in
;; the api-post-controller can do any other actions

;; --- API Multimethod Declarations ---

(defmulti api-event
  ;; event is the name of the event
  ;; message is one of {:success :failure} (1st value in the channel vector)
  ;; args is the 2nd value in the channel vector)
  ;; state is current state of the app
  ;; return value is the new state
  (fn [event message {:as args :keys [status method response]} state] [event message]))

(defmulti post-api-event!
  (fn [event message {:as args :keys [status method response]} previous-state current-state] [event message]))

;; --- API Multimethod Implementations ---

(defmethod api-event :default
  [event message {:as args :keys [status method response]} state]
  ;; subdispatching for state defaults
  (let [submethod (get-method api-event [:default status])]
    (if submethod
      (submethod event message status args state)
      (do (merror "Unknown api: " message args)
          state))))

(defmethod post-api-event! :default
  [event message {:as args :keys [status method response]} previous-state current-state]
  ;; subdispatching for state defaults
  (let [submethod (get-method post-api-event! [:default status])]
    (if submethod
      (submethod event message status args previous-state current-state)
      (merror "Unknown api: " event message status args))))

(defmethod api-event [:default :success]
  [event message {:as args :keys [status method response]} state]
  (mlog "No api for" [message status])
  state)

(defmethod post-api-event! [:default :success]
  [event message {:as args :keys [status method response]} previous-state current-state]
  (mlog "No post-api for: " [message status]))

(defmethod api-event [:default :failed]
  [event message {:as args :keys [status method response]} state]
  ;; XXX update the error message
  (mlog "No api for" [message status])
  state)

(defmethod api-event [:issue :success]
  [event message {:as args :keys [status method response]} state]
;  (assoc-in state [:gh-cache :issues 1024] response)
  )