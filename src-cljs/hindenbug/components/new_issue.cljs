(ns hindenbug.components.new-issue
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [clojure.string :as str]
            [hindenbug.util.common-words :as common]
            [cljs.core.async :as async :refer [put!]])
  (:require-macros [hindenbug.utils :refer (inspect defrender)]))

(defn terms [data]
  (let [words (if (-> data :search :include-last-term?)
                (-> data :search :terms)
                (butlast (-> data :search :terms)))]
    (remove common/words words)))

(defrender issue-summary [issue owner]
  (let [{:keys [user closed_by number body title comments state labels]} issue]
    (html
     [:div
      [:table
       [:tr
        [:td {:col-span 5} title]]
       [:tr
        [:td number]
        [:td (-> user :login)]
        [:td  state]
        [:td comments]
        [:td (map :name labels)]]]])))

(defrender new-issue [data owner]
  (let [c (-> data :comms :controls)]
    (html
     [:div#create-issue
      ;; TODO: every time a word is finished, and the word isn't a small word, do
      ;; a search. Cache all the searches with timestamps. Limit all searches to 5
      ;; seconds, and don't repeat within 5 minutes. Combine searches for all the
      ;; words from the title. Show in the sidebar.
      [:div#enter
       [:h2 "Create a new issue"]
       [:div [:input#title {:type "text"
                            :on-blur #(put! c [:search-blur {:value (.. % -target -value)}])
                            :on-key-up #(put! c [:search-key-up {:key (.. % -which)
                                                                 :value (.. % -target -value)}])}]]
       [:div [:textarea#body]]]

      [:div#search
       [:h2 "output"]
       [:h3 (str "search terms: " (str/join " " (terms data)))]
       (let [terms (terms data)
             all (map #(-> data :search :cache inspect (get %) :body :items) terms)
             all (-> all flatten concat)]
         (doall (for [result all]
                  (om/build issue-summary result))))]])))
