(ns hindenbug.util.gh
  (:require [cemerick.url :as url]
            [cljs-http.client :as http]
            [clojure.string :as str]
            [hindenbug.util.json :as json]
            [goog.string :as gstring]
            [goog.string.format]
            [cljs.core.async :refer (take!)])
  (:require-macros [hindenbug.utils :refer [inspect]]))

(def ^:dynamic url "https://api.github.com/")
(def ^:dynamic defaults {})

(defn format-url
  "Creates a URL out of end-point and positional. Called URLEncoder/encode on
   the elements of positional and then formats them in."
  [end-point positional]
  (str url (apply gstring/format end-point (map url/url-encode positional))))

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
        proper-query (dissoc query "auth" "oauth_token" "all_pages" "accept" "user_agent")
        req (if (#{:post :put :delete} method)
              (assoc req :json-params (or (proper-query "raw") proper-query))
              (assoc req :query-params proper-query))]
    req))

(defn update-req
  "Given a clj-http request, and a 'next' url string, merge the next url into the request"
  [req url]
  (let [url-map (url/url url)]
    (assoc-in req [:query-params] (-> url-map :query))))

(defn query-map
  "Merge defaults, turn keywords into strings, and replace hyphens with underscores."
  [entries]
  (into {}
        (for [[k v] (concat defaults entries)]
          [(.replace (name k) "-" "_") v])))

(defn extract-useful-meta
  [h]
  (let [{:strs [etag last-modified x-ratelimit-limit x-ratelimit-remaining
                x-poll-interval]}
        h]
    {:etag etag :last-modified last-modified
     :call-limit (when x-ratelimit-limit (json/parse x-ratelimit-limit))
     :call-remaining (when x-ratelimit-remaining (json/parse x-ratelimit-remaining))
     :poll-interval (when x-poll-interval (json/parse x-poll-interval))}))

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

(defn safe-parse
  "Takes a response and checks for certain status codes. If 204,
  return nil.  If 400, 401, 204, 422, 403, 404 or 500, return the
  original response with the body parsed as json. Otherwise, parse and
  return the body if json, or return the body if raw."
  [{:keys [headers status body] :as resp}]
  (cond
   (= 304 status)
   ::not-modified
   (#{400 401 204 422 403 404 500} status)
   (update-in resp [:body] json/parse)
   :else (let [links (parse-links (get headers "link" ""))
               content-type (get headers "content-type")
               metadata (extract-useful-meta headers)]
           (if (pos? (.indexOf content-type "raw"))
             body
             (if (map? body)
               (with-meta body {:links links :api-meta metadata})
               (with-meta (map #(with-meta % metadata) body)
                 {:links links :api-meta metadata}))))))

(defn api-call
  "(Doesn't respect `:all-pages`...)"
  ([method end-point] (api-call method end-point nil nil))
  ([method end-point positional] (api-call method end-point positional nil))
  ([method end-point positional query]
     (let [query (query-map query)
           all-pages? (query "all_pages")
           req (make-request method end-point positional query)
           resp (http/request req)]
       (take! resp safe-parse))))

(defn- join-labels [m]
  (if (:labels m)
    (update-in m [:labels] (partial str/join ","))
    m))

(defn issues
  "List issues for a repository.
   Options are:
     milestone -- Milestone number,
                  none: no milestone,
                  *: any milestone.
     assignee  -- A username,
                  none: no assigned user,
                  *: any assigned user.
     mentioned -- A username.
     state     -- open (default), closed.
     labels    -- An array of label names.
     sort      -- created (default), updated, comments.
     direction -- asc: ascending,
                  desc (default): descending.
     since     -- String ISO 8601 timestamp."
  [user repo & [options]]
  (api-call :get "repos/%s/%s/issues" [user repo] (join-labels options)))

(defn issue
  "Get a single issue"
  [user repo number & [options]]
  (api-call :get "repos/%s/%s/issues/%d" [user repo number] (join-labels options)))
