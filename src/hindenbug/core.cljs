(ns hindenbug.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cemerick.url :as url]
            [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

(enable-console-print!)

(def navigation-ch
  (chan))

(def app-state
  (atom {:channels {:navigation navigation-ch}
         :issues {}
         :current-team nil}))

(defn team-overview [data owner]
  (dom/h1 nil (-> data :current-team)))

(defn team-listing [data owner]
  (reify
   om/IRender
   (render [this]
     (if-let [current-team (-> data :current-team)]
       (team-overview [data owner])
       (dom/h1 nil "Teams")))))

(defn respond-to-oauth-code []
  (let [code (-> js/document.URL url/url :query (get "code"))

        ;; bug in routing adds a '/' suffix
        code (.substring code 0 (- (count code) 1))]

    (print code)))

(om/root team-listing app-state
  {:target (. js/document (getElementById "app"))})

(defn navigation-handler
  [value state]
  (print "Navigate here"))

(go (while true
      (alt!
        navigation-ch ([v] (navigation-handler v app-state))
        (async/timeout 10000) (do
                                (print "Sleeping")))))

(respond-to-oauth-code)
