(ns hindenbug.routes
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [clojure.string :as str]
            [goog.events :as events]
            [hindenbug.login :as login]
            [hindenbug.utils :as utils :include-macros true]
            [secretary.core :as sec :include-macros true :refer [defroute]])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

;; TODO: make this handle params
;; Creates a route that will ignore fragments and add them to params as {:_fragment "#fragment"}
(defrecord FragmentRoute [route]
  sec/IRenderRoute
  (render-route [this]
    route))

(extend-protocol sec/IRouteMatches
  FragmentRoute
  (route-matches [this route]
    (let [[normal-route fragment] (str/split route #"#" 2)]
      (when-let [match (sec/route-matches (sec/compile-route (:route this)) normal-route)]
        (merge match
               (when fragment {:_fragment fragment}))))))

(defn define-routes! [state]
  (let [nav-ch (get-in @state [:comms :nav])]
    (defroute v1-root (FragmentRoute. "/") {:as params}
      (if (login/logged-in?)
        (put! nav-ch [:teams-overview])
        (put! nav-ch [:login-screen])))

    (defroute v1-root (FragmentRoute. "/login") {:as params}
      (put! nav-ch [:login]))

    (defroute v1-root (FragmentRoute. "/logout") {:as params}
      (put! nav-ch [:logout]))

    (defroute "/issues/:id" {:as params}
      (put! nav-ch [:dashboard (:id params)]))

    (defroute "/issues/new" {:as params}
      (put! nav-ch [:create-issue]))))
