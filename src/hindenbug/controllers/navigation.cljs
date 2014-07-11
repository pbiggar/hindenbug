(ns hindenbug.controllers.navigation
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close!]]
            [clojure.string :as str]
            [goog.dom]
            [goog.string :as gstring]
            [goog.style])
  (:require-macros [hindenbug.utils :refer [inspect]]
                   [dommy.macros :refer [sel sel1]]
                   [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

;; TODO we could really use some middleware here, so that we don't forget to
;;      assoc things in state on every handler
;;      We could also use a declarative way to specify each page.

;; --- Helper Methods ---

(defn set-page-title! [& [title]]
  (set! (.-title js/document) title))

(defn scroll-to-fragment!
  "Scrolls to the element with id of fragment, if one exists"
  [fragment]
  (when-let [node (goog.dom.getElement fragment)]
    (let [main (goog.dom.getElementByClass "app-main")
          node-top (goog.style/getPageOffsetTop node)
          main-top (goog.style/getPageOffsetTop main)
          main-scroll (.-scrollTop main)]
      (set! (.-scrollTop main) (+ main-scroll (- node-top main-top))))))

(defn scroll!
  "Scrolls to fragment if the url had one, or scrolls to the top of the page"
  [args]
  (if (:_fragment args)
    ;; give the page time to render
    (js/requestAnimationFrame #(scroll-to-fragment! (:_fragment args)))
    (js/requestAnimationFrame #(set! (.-scrollTop (sel1 "main.app-main")) 0))))

;; --- Navigation Multimethod Declarations ---

(defmulti navigated-to
  (fn [history-imp navigation-point args state] navigation-point))

(defmulti post-navigated-to!
  (fn [history-imp navigation-point args previous-state current-state]
    navigation-point))

;; --- Navigation Multimethod Implementations ---

(defmethod navigated-to :default
  [history-imp navigation-point args state]
  (-> state
      (assoc :navigation-point navigation-point
             :navigation-data args)))

(defmethod post-navigated-to! :default
  [history-imp navigation-point args previous-state current-state]
  (set-page-title! (str/capitalize (name navigation-point)))
  (scroll! args))

(defmethod post-navigated-to! :navigate!
  [history-imp navigation-point {:keys [path replace-token?]} previous-state current-state]
  (let [path (if (= \/ (first path))
               (subs path 1)
               path)]
    (if replace-token? ;; Don't break the back button if we want to redirect someone
      (.replaceToken history-imp path)
      (.setToken history-imp path))))

(defmethod navigated-to :error
  [history-imp navigation-point {:keys [status] :as args} state]
  (-> state
      (assoc :navigation-point navigation-point
             :navigation-data args)))

(defmethod post-navigated-to! :error
  [history-imp navigation-point {:keys [status] :as args} previous-state current-state]
  (set-page-title! (condp = status
                     401 "Login required"
                     404 "Page not found"
                     500 "Internal server error"
                     "Something unexpected happened")))


(defmethod navigated-to :dashboard
  [history-imp navigation-point args state]
  (js/alert "dashboard"))

(defmethod navigated-to :login-screen
  [history-imp navigation-point args state]
  (js/alert "login-screen"))

(defmethod navigated-to :login
  [history-imp navigation-point args state]
  (js/alert "login"))