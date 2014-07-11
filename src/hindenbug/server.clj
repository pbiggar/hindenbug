(ns hindenbug.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :as reload]
            [ring.middleware.file :as file]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.resource :as resource]
            [hiccup.core :as hiccup]))

(defn dev-mode? []
  (-> "HINDENBUG_PRODUCTION" System/getenv nil?))

(def template
  (hiccup/html
   [:html
    [:head
     [:link {:rel :stylesheet
             :href "http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css"
             :type "text/css"}]]
    [:body
     [:script {:src "http://fb.me/react-0.9.0.js" :type "text/javascript"}]
     (if (dev-mode?)
       [:script {:src "out/goog/base.js" :type "text/javascript"}]
       [:script {:src "js/hindenbug.js" :type "text/javascript"}])

     (when (dev-mode?)
       [:script {:type "text/javascript"} "goog.require(\"hindenbug.core\");"])]]))


(defn handler [req]
  (condp = (:uri req)
    "/" {:status 200
         :headers {"Content-Type" "text/html"}
         :body template}
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str req)}))

(defn wrap-file
  [handler]
  (if (dev-mode?)
    (file/wrap-file handler "./")
    (resource/wrap-resource handler "public")))

(def app
  (-> handler
      wrap-file
      file-info/wrap-file-info
      reload/wrap-reload))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))