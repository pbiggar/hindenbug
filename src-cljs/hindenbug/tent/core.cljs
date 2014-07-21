(ns hindenbug.tent.core
  "A short version of tentacles, rewritten for cljs"
  (:require [cemerick.url :as url]
            [cljs-http.core :as http-core]
            [cljs-http.client :as http-client]
            [cljs.core.async :refer (take! put! <!)]
            [clojure.string :as str]
            [goog.string :as gstring]
            [goog.string.format]
            [hindenbug.util.json :as json]
            [hindenbug.util.core :refer (apply-map)])
  (:require-macros [hindenbug.utils :refer (inspect)]
                   [cljs.core.async.macros :refer (go)]))

;;; cljs-http.client/request insists on doing lots for us, which we don't need.
;;; Make a request which does less.
(def request
  (-> http-core/request
      http-client/wrap-accept
      http-client/wrap-content-type
      http-client/wrap-json-params
      http-client/wrap-query-params
      http-client/wrap-oauth
      http-client/wrap-android-cors-bugfix
      http-client/wrap-method
      http-client/wrap-url))

;;; This file is a copy of tentacles.core. See NOTES for any changes.

(def ^:dynamic url "https://api.github.com/")
(def ^:dynamic defaults {})

(defn query-map
  "Merge defaults, turn keywords into strings, and replace hyphens with underscores."
  [entries]
  (into {}
        (for [[k v] (concat defaults entries)]
          [(.replace (name k) "-" "_") v])))

;;; NOTES: json refers to our json, not clj-json
(defn parse-json
  "Same as json/parse-string but handles nil gracefully."
  [s] (when s (json/parse-string s true)))

(defn parse-link [link]
  (let [[_ url] (re-find #"<(.*)>" link)
        [_ rel] (re-find #"rel=\"(.*)\"" link)]
    [(keyword rel) url]))

(defn parse-links
  "Takes the content of the link header from a github resp, returns a map of links"
  [link-body]
  (->> (str/split link-body #",")
       (map parse-link)
       (into {})))

;;; NOTES: use js/parseInt instead of parseLong. All values should be in
(defn extract-useful-meta
  [h]
  (let [{:strs [etag last-modified x-ratelimit-limit x-ratelimit-remaining
                x-poll-interval]}
        h]
    {:etag etag :last-modified last-modified
     :call-limit (when x-ratelimit-limit (js/parseInt x-ratelimit-limit))
     :call-remaining (when x-ratelimit-remaining (js/parseInt x-ratelimit-remaining))
     :poll-interval (when x-poll-interval (js/parseInt x-poll-interval))}))

(defn api-meta
  [obj]
  (:api-meta (meta obj)))

(defn safe-parse
  "Takes a response and checks for certain status codes. If 204, return nil.
   If 400, 401, 204, 422, 403, 404 or 500, return the original response with the body parsed
   as json. Otherwise, parse and return the body if json, or return the body if raw."
  [{:keys [headers status body] :as resp}]
  (cond
   (= 304 status)
   ::not-modified
   (#{400 401 204 422 403 404 500} status)
   (update-in resp [:body] parse-json)
   :else (let [links (parse-links (get headers "link" ""))
               content-type (get headers "content-type")
               metadata (extract-useful-meta headers)]
           (if-not (.contains content-type "raw")
             (let [parsed (parse-json body)]
               (if (map? parsed)
                 (with-meta parsed {:links links :api-meta metadata})
                 (with-meta (map #(with-meta % metadata) parsed)
                   {:links links :api-meta metadata})))
             body))))

;;; NOTES: just not actually called from anywhere at the moment
(defn update-req
  "Given a clj-http request, and a 'next' url string, merge the next url into the request"
  [req url]
  (let [url-map (url/url url)]
    (assoc-in req [:query-params] (-> url-map :query))))

;;; NOTES: not changed, just doesn't make much sense
(defn no-content?
  "Takes a response and returns true if it is a 204 response, false otherwise."
  [x] (= (:status x) 204))

;;; NOTES: use url/url-encode instead
(defn format-url
  "Creates a URL out of end-point and positional. Called URLEncoder/encode on
   the elements of positional and then formats them in."
  [end-point positional]
  (str url (apply gstring/format end-point (map url/url-encode positional))))

;;; NOTES: use :json-params and with-crednentials
(defn make-request [method end-point positional
                    {:strs [auth throw_exceptions follow_redirects accept
                            oauth_token etag if_modified_since user_agent]
                     :or {follow_redirects true throw_exceptions false}
                     :as query}]
  (let [req (merge-with merge
                        {:url (format-url end-point positional)
                         :basic-auth auth
                         :throw-exceptions throw_exceptions
                         :follow-redirects follow_redirects
                         :method method
                         :with-credentials? false}
                        (when accept
                          {:headers {"Accept" accept}})
                        (when oauth_token
                          {:headers {"Authorization" (str "token " oauth_token)}})
                        (when etag
                          {:headers {"if-None-Match" etag}})
                        (when user_agent
                          {:headers {"User-Agent" user_agent}})
                        (when if_modified_since
                          {:headers {"if-Modified-Since" if_modified_since}}))
        proper-query (dissoc query "auth" "oauth_token" "all_pages" "accept" "user_agent" "callback" "calling_context")
        req (if (#{:post :put :delete} method)
              (assoc req :json-params (or (proper-query "raw") proper-query))
              (assoc req :query-params proper-query))]
    req))

;;; NOTES: handle asyncronously, using :callback and :calling-context from query
(defn api-call
  ([method end-point] (api-call method end-point nil nil))
  ([method end-point positional] (api-call method end-point positional nil))
  ([method end-point positional query]
     (let [query (query-map query)
           all-pages? (query "all_pages")
           req (make-request method end-point positional query)

           ;;; TODO: implement all_pages
           ;; exec-request-one (fn exec-request-one [req]
           ;;                    (safe-parse (http/request req)))
           ;; exec-request (fn exec-request [req]
           ;;                (let [resp (exec-request-one req)]
           ;;                  (if (and all-pages? (-> resp meta :links :next))
           ;;                    (let [new-req (update-req req (-> resp meta :links :next))]
           ;;                      (lazy-cat resp (exec-request new-req)))
           ;;                    resp)))

           ;;; async reimplementation
           callback (:callback query)
           calling-context (or (:calling-context query))]
       (go (let [response (<! (request req))
                 parsed (safe-parse response)]
             (apply-map callback parsed calling-context))))))

;;; NOTES: not changed, and so won't actually work
(defn raw-api-call
  ([method end-point] (raw-api-call method end-point nil nil))
  ([method end-point positional] (raw-api-call method end-point positional nil))
  ([method end-point positional query]
     (let [query (query-map query)
           all-pages? (query "all_pages")
           req (make-request method end-point positional query)]
       (http-client/request req))))

;;; NOTES: not changed, but useless
(defn environ-auth
  "Lookup :gh-username and :gh-password in environ (~/.lein/profiles.clj or .lein-env) and return a string auth.
   Usage: (users/me {:auth (environ-auth)})"
  [env]
  (str (:gh-username env ) ":" (:gh-password env)))

(defn rate-limit
  ([] (api-call :get "rate_limit"))
  ([opts] (api-call :get "rate_limit" nil opts)))

;;; NOTES: moved into core.clj
;; (defmacro with-defaults [options & body]
;;  `(binding [defaults ~options]
;;     ~@body))

;; (defmacro with-url [new-url & body]
;;  `(binding [url ~new-url]
;;     ~@body))
