(ns hindenbug.core
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [cljs-time.core :as time]
            [hindenbug.utils :as utils :refer [mlog merror]]
            [hindenbug.app :as app]
            [weasel.repl :as ws-repl]
            [clojure.browser.repl :as repl]
            [clojure.string :as string]
            [dommy.core :as dommy]
            [goog.dom]
            [goog.dom.DomHelper]
            [hindenbug.controllers.controls :as controls-con]
            [hindenbug.controllers.navigation :as nav-con]
            [hindenbug.controllers.api :as api-con]
            [hindenbug.routes :as routes]
            [hindenbug.state :as state]
            [goog.events]
            [om.core :as om :include-macros true]
            [hindenbug.history :as history]
            [secretary.core :as sec :include-macros true :refer [defroute]])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                   [hindenbug.utils :refer [inspect timing swallow-errors]])
  (:use-macros [dommy.macros :only [node sel sel1]]))

(enable-console-print!)

;; Overcome some of the browser limitations around DnD
(def mouse-move-ch
  (chan (sliding-buffer 1)))

(def mouse-down-ch
  (chan (sliding-buffer 1)))

(def mouse-up-ch
  (chan (sliding-buffer 1)))

(js/window.addEventListener "mousedown" #(put! mouse-down-ch %))
(js/window.addEventListener "mouseup"   #(put! mouse-up-ch   %))
(js/window.addEventListener "mousemove" #(put! mouse-move-ch %))

(def controls-ch
  (chan))

(def api-ch
  (chan))

(def error-ch
  (chan))

(def navigation-ch
  (chan))

(defn app-state []
  (atom (assoc (state/initial-state)
          :render-context {}
          :comms {:controls  controls-ch
                  :api       api-ch
                  :errors    error-ch
                  :nav       navigation-ch
                  :controls-mult (async/mult controls-ch)
                  :api-mult (async/mult api-ch)
                  :errors-mult (async/mult error-ch)
                  :nav-mult (async/mult navigation-ch)
                  :mouse-move {:ch mouse-move-ch
                               :mult (async/mult mouse-move-ch)}
                  :mouse-down {:ch mouse-down-ch
                               :mult (async/mult mouse-down-ch)}
                  :mouse-up {:ch mouse-up-ch
                             :mult (async/mult mouse-up-ch)}})))

(defn log-channels?
  "Log channels in development, can be overridden by the log-channels query param"
  []
  (if (nil? (:log-channels? utils/initial-query-map))
    true
    (:log-channels? utils/initial-query-map)))

(defn controls-handler
  [value state container]
  (when (log-channels?)
    (mlog "Controls Verbose: " value))
  (swallow-errors
   (let [previous-state @state]
     (swap! state (partial controls-con/control-event container (first value) (second value)))
     (controls-con/post-control-event! container (first value) (second value) previous-state @state))))

(defn nav-handler
  [value state history]
  (when (log-channels?)
    (mlog "Navigation Verbose: " value))
  (swallow-errors
   (let [previous-state @state]
     (swap! state (partial nav-con/navigated-to history (first value) (second value)))
     (nav-con/post-navigated-to! history (first value) (second value) previous-state @state))))

(defn api-handler
  [value state container]
  (when (log-channels?)
    (mlog "API Verbose: " (first value) (second value) (utils/third value)))
  (swallow-errors
   (let [previous-state @state
         message (first value)
         status (second value)
         api-data (utils/third value)]
     (swap! state (partial api-con/api-event container message status api-data))
     (api-con/post-api-event! container message status api-data previous-state @state))))

(defn main [state top-level-node]
  (let [comms       (:comms @state)
        container   (sel1 top-level-node "#om-app")
        uri-path    (.getPath utils/parsed-uri)
        history-path "/"
        history-imp (history/new-history-imp top-level-node)
        controls-tap (chan)
        nav-tap (chan)
        api-tap (chan)]
    (routes/define-routes! state)

    (om/root
     app/app
     app-state
     {:target (. js/document (getElementById "app"))
      :shared {:comms comms}})

    (async/tap (:controls-mult comms) controls-tap)
    (async/tap (:nav-mult comms) nav-tap)
    (async/tap (:api-mult comms) api-tap)

    (go (while true
          (alt!
           controls-tap ([v] (controls-handler v state container))
           nav-tap ([v] (nav-handler v state history-imp))
           api-tap ([v] (api-handler v state container))
           ;; Capture the current history for playback in the absence
           ;; of a server to store it
           (async/timeout 10000) (do (print "TODO: print out history: ")))))))

(defn setup-browser-repl [repl-url]
  (when repl-url
    (repl/connect repl-url))
  ;; this is harmless if it fails
  (ws-repl/connect "ws://localhost:9001" :verbose true)
  ;; the repl tries to take over *out*, workaround for
  ;; https://github.com/cemerick/austin/issues/49
  (js/setInterval #(enable-console-print!) 1000))

(defn dispatch-to-current-location! []
  (let [uri (goog.Uri. js/document.location.href)]
    (sec/dispatch! (str (.getPath uri)
                        (when-not (string/blank? (.getQuery uri))
                          (str "?" (.getQuery uri)))
                        (when-not (string/blank? (.getFragment uri))
                          (str "#" (.getFragment uri)))))))


(defn ^:export setup! []
  (let [state (app-state)]
    ;; globally define the state so that we can get to it for debugging
    (def debug-state state)
    (main state (sel1 :body))
    (dispatch-to-current-location!)
    (try
      (setup-browser-repl (get-in @state [:render-context :browser_connected_repl_url]))
      (catch js/error e
        (merror e)))))


(setup!)
