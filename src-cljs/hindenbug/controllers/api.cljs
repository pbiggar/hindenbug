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
  ;; event is the name of the event  (1st value in the channel vector)
  ;; message is one of {:success :failure} (2nd value in the channel vector)
  ;; api-data is the response map (3rd value in the channel vector)
  ;; state is current state of the app
  ;; return value is the new state
  (fn [container event message api-data state] [event message]))

(defmulti post-api-event!
  (fn [container event message api-data previous-state current-state] [event message]))

;; --- API Multimethod Implementations ---

(defmethod api-event :default
  [container event message api-data state]
  ;; subdispatching for state defaults
  (let [submethod (get-method api-event [:default message])]
    (if submethod
      (submethod container event message api-data state)
      (do (merror "Unknown api: " event message)
          state))))

(defmethod post-api-event! :default
  [container event message api-data previous-state current-state]
  ;; subdispatching for state defaults
  (let [submethod (get-method post-api-event! [:default message])]
    (if submethod
      (submethod container :default message api-data previous-state current-state)
      (merror "Unknown api: " event message))))

(defmethod api-event [:default :success]
  [container event message api-data state]
  (mlog "No api for" [event message])
  state)

(defmethod post-api-event! [:default :success]
  [container event message api-data previous-state current-state]
  (mlog "No post-api for: " [event message]))

(defmethod api-event [:default :failed]
  [container event message api-data state]
  ;; XXX update the error message
  (mlog "No api for" [event message])
  state)

(defmethod api-event [:issue :success]
  [containers event message {:as api-data :keys [status method response]} state]
  (assoc-in state [:gh-cache :issues 1024] response))