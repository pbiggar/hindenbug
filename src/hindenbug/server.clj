(ns hindenbug.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :as reload]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [ring.middleware.file :as file]
            [ring.middleware.file-info :as file-info]
            [ring.middleware.resource :as resource]
            [ring.middleware.session :as session]
            [ring.middleware.session.store :as session-store]
            [ring.util.response :as response]
            [ring.middleware.session.cookie :as cookie]
            [clj-time.core :as time]
            [clj-time.format :as time-format]
            [cemerick.url :as url]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(def github-client-tokens
  {:client_id (System/getenv "GITHUB_CLIENT_ID")
   :client_secret (System/getenv "GITHUB_CLIENT_SECRET")})

(when (or (-> github-client-tokens :client_id nil?)
          (-> github-client-tokens :client_secret nil?))
  (print "Set GITHUB_CLIENT_ID and GITHUB_CLIENT_SECRET for your application")
  (System/exit -1))


(defn dev-mode? []
  (-> "HINDENBUG_PRODUCTION" System/getenv nil?))

(def template
  (hiccup/html
   (page/html5
    [:html
     [:head (page/include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css")]
     [:body
      [:div#app]
      (page/include-js "//fb.me/react-0.9.0.js")

      (if (dev-mode?)
        (page/include-js "js/goog/base.js"))

      (page/include-js "js/hindenbug.js")
      (when dev-mode?
        [:script {:type "text/javascript"} "goog.require(\"hindenbug.core\");"])]])))

(defn fetch-github-token [code]
  (let [response (http/post "https://github.com/login/oauth/access_token"
                            {:form-params (assoc github-client-tokens :code code)
                             :accept :json})
        {:keys [access-token error]} (json/parse-string (:body response) true)]
    (if (or error (not access-token))
      (do
        (print error)
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (str "Error found: " error)})
      access_token)))

(deftype UnencryptedJSONCookieStore []
  session-store/SessionStore
  (read-session [_ data]
    (json/decode data))
  (write-session [_ _ data]
    (json/encode data))
  (delete-session [_ _]
    (json/encode {})))

(defn login [req]
  (print req)
  (let [code (-> req :query-string url/query->map :code)
        token (fetch-github-token code)]
    {:status 302
     :headers {"Location" "/"}
     :session {:token token :code code}}))

(defn handler [req]
  (condp = (:uri req)
    "/"
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body template}
    "/login" (login req)
    nil))

(defn wrap-file
  [handler]
  (if (dev-mode?)
    (resource/wrap-resource handler "development")
    (resource/wrap-resource handler "public")))

(def app
  (-> handler
      wrap-file
      file-info/wrap-file-info
      (session/wrap-session {:cookie-name "hindenbug-session"
                             :root "/"
                             :store (UnencryptedJSONCookieStore.)
                             :cookie-attrs {:http-only true
                                            :max-age (* 60 60 24 365)
                                            ;; expire one year after the server starts up
                                            :expires (time-format/unparse
                                                      (:rfc822 time-format/formatters)
                                                      (time/from-now (time/years 1)))
                                            :secure (not (dev-mode?))}})
      reload/wrap-reload))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
