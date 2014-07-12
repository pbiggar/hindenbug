(ns hindenbug.app
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer close! put!]]
            [om.dom :as dom :include-macros true]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [hindenbug.utils :refer [mlog]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(def sample-issues
  (js->clj "[{
    \"url\": \"https://api.github.com/repos/octocat/Hello-World/issues/1347\",
    \"html_url\": \"https://github.com/octocat/Hello-World/issues/1347\",
    \"number\": 1347,
    \"state\": \"open\",
    \"title\": \"Found a bug\",
    \"body\": \"I'm having a problem with this.\",
    \"user\": {
      \"login\": \"octocat\",
      \"id\": 1,
      \"avatar_url\": \"https://github.com/images/error/octocat_happy.gif\",
      \"gravatar_id\": \"somehexcode\",
      \"url\": \"https://api.github.com/users/octocat\",
      \"html_url\": \"https://github.com/octocat\",
      \"followers_url\": \"https://api.github.com/users/octocat/followers\",
      \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",
      \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",
      \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",
      \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",
      \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",
      \"repos_url\": \"https://api.github.com/users/octocat/repos\",
      \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",
      \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",
      \"type\": \"User\",
      \"site_admin\": false
    },
    \"labels\": [
      {
        \"url\": \"https://api.github.com/repos/octocat/Hello-World/labels/bug\",
        \"name\": \"bug\",
        \"color\": \"f29513\"
      }
    ],
    \"assignee\": {
      \"login\": \"octocat\",
      \"id\": 1,
      \"avatar_url\": \"https://github.com/images/error/octocat_happy.gif\",
      \"gravatar_id\": \"somehexcode\",
      \"url\": \"https://api.github.com/users/octocat\",
      \"html_url\": \"https://github.com/octocat\",
      \"followers_url\": \"https://api.github.com/users/octocat/followers\",
      \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",
      \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",
      \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",
      \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",
      \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",
      \"repos_url\": \"https://api.github.com/users/octocat/repos\",
      \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",
      \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",
      \"type\": \"User\",
      \"site_admin\": false
    },
    \"milestone\": {
      \"url\": \"https://api.github.com/repos/octocat/Hello-World/milestones/1\",
      \"number\": 1,
      \"state\": \"open\",
      \"title\": \"v1.0\",
      \"description\": \"\",
      \"creator\": {
        \"login\": \"octocat\",
        \"id\": 1,
        \"avatar_url\": \"https://github.com/images/error/octocat_happy.gif\",
        \"gravatar_id\": \"somehexcode\",
        \"url\": \"https://api.github.com/users/octocat\",
        \"html_url\": \"https://github.com/octocat\",
        \"followers_url\": \"https://api.github.com/users/octocat/followers\",
        \"following_url\": \"https://api.github.com/users/octocat/following{/other_user}\",
        \"gists_url\": \"https://api.github.com/users/octocat/gists{/gist_id}\",
        \"starred_url\": \"https://api.github.com/users/octocat/starred{/owner}{/repo}\",
        \"subscriptions_url\": \"https://api.github.com/users/octocat/subscriptions\",
        \"organizations_url\": \"https://api.github.com/users/octocat/orgs\",
        \"repos_url\": \"https://api.github.com/users/octocat/repos\",
        \"events_url\": \"https://api.github.com/users/octocat/events{/privacy}\",
        \"received_events_url\": \"https://api.github.com/users/octocat/received_events\",
        \"type\": \"User\",
        \"site_admin\": false
      },
      \"open_issues\": 4,
      \"closed_issues\": 8,
      \"created_at\": \"2011-04-10T20:09:31Z\",
      \"updated_at\": \"2014-03-03T18:58:10Z\",
      \"due_on\": null
    },
    \"comments\": 0,
    \"pull_request\": {
      \"url\": \"https://api.github.com/repos/octocat/Hello-World/pulls/1347\",
      \"html_url\": \"https://github.com/octocat/Hello-World/pull/1347\",
      \"diff_url\": \"https://github.com/octocat/Hello-World/pull/1347.diff\",
      \"patch_url\": \"https://github.com/octocat/Hello-World/pull/1347.patch\"
    },
    \"closed_at\": null,
    \"created_at\": \"2011-04-22T13:33:48Z\",
    \"updated_at\": \"2011-04-22T13:33:48Z\"
  }
]"))


(defrender teams-overview [data owner]
  (html
   [:div
    [:h1 "Teams"]
    (map
     (fn [issue] 
       [:h2
        [:a
         {:href (:number issue)}
         (:title issue)]])
     sample-issues)]))

(defrender dashboard [data owner]
  (html [:div]))

(defrender login-screen []
  (html
   [:div
    [:a {:href "/login"} "Login"]
    [:a {:href "/logout"} "Logout"]]))

(defrender create-issue []
  (html [:div]))

(defrender blank-screen []
  (html
   [:div "empty"]))

(defn dominant-component [data owner]
  (print "New dominant component: " (get-in data [:navigation-point]))
  (condp = (get-in data [:navigation-point])
    :login-screen login-screen
    :teams-overview teams-overview
    :dashboard dashboard
    :create-issue create-issue
    nil blank-screen))

(defrender app [data owner]
  (om/build
   (dominant-component data owner)
   data))
