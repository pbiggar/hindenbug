(ns hindenbug.server
  (:require [ring.adapter.jetty :as jetty]))

(def template
  "<html>
  <head>
  <link rel=\"stylesheet\" href=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css\" type=\"text/css\">
  </head>
  <body>
  <div id=\"app\"></div>
  <script src=\"http://fb.me/react-0.9.0.js\"></script>
  <script src=\"out/goog/base.js\" type=\"text/javascript\"></script>
  <script src=\"hindenbug.js\" type=\"text/javascript\"></script>
  <script type=\"text/javascript\">goog.require(\"hindenbug.core\");</script>
  </body>
  </html>")


(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body template})

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))