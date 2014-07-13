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
     [:head (page/include-css "/css/bootstrap-3.2.0.min.css")]
     [:body
      [:div#app]
      (page/include-js "/js/react-0.9.0.js")

      (if (dev-mode?)
        (page/include-js "/js/goog/base.js"))

      (page/include-js "/js/hindenbug.js")
      (when dev-mode?
        [:script {:type "text/javascript"} "goog.require(\"hindenbug.core\");"])]])))

(defn fetch-github-token [code]
  (let [response (http/post "https://github.com/login/oauth/access_token"
                            {:form-params (assoc github-client-tokens :code code)
                             :accept :json})]
    (json/parse-string (:body response) true)))

(deftype UnencryptedJSONCookieStore []
  session-store/SessionStore
  (read-session [_ data]
    (json/decode data))
  (write-session [_ _ data]
    (json/encode data))
  (delete-session [_ _]
    (json/encode {})))

;; TODO: logout where we delete the cookie

(defn auth [req]
  (let [code (-> req :query-string url/query->map (get "code"))
        {:keys [error_description error_uri access_token error]} (fetch-github-token code)]
    (if (or error error_description error_uri (not access_token))
      {:status 500
       :headers {"Content-Type" "text/html"}
       :body (str "Error found: " error ", " error_description ", " error_uri)}
      {:status 302
       :headers {"Location" "/"}
       :session {:token access_token}})))

(defn login [req]
  {:status 302
   :headers {"Location" "https://github.com/login/oauth/authorize?client_id=40a61e0d29bc72207572&scope=repo"}})

(defn logout [req]
  {:status 302
   :headers {"Location" "/"}
   :session nil})

(defn router [req]
  (condp re-matches (:uri req)
    #"/auth" (auth req)
    #"/login" (login req)
    #"/logout" (logout req)
    #"/js/" nil ; leave to file-wrap
    #"/css/" nil ; leave to file-wrap
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body template}))


(defn wrap-file
  [handler]
  (if (dev-mode?)
    (resource/wrap-resource handler "development")
    (resource/wrap-resource handler "public")))

;; TODO add gzip
(def app
  (-> router
      wrap-file
      (resource/wrap-resource "vendor")
      file-info/wrap-file-info
      (session/wrap-session {:cookie-name "hindenbug-session"
                             :root "/"
                             :store (UnencryptedJSONCookieStore.)
                             :cookie-attrs {:max-age (* 60 60 24 365)

                                            ;; need to access via JS
                                            :http-only false

                                            ;; expire one year after the server starts up
                                            :expires (time-format/unparse
                                                      (:rfc822 time-format/formatters)
                                                      (time/from-now (time/years 1)))

                                            :secure (not (dev-mode?))}})
      reload/wrap-reload))

(defn -main [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
