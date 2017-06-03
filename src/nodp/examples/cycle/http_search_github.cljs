(ns nodp.examples.cycle.http-search-github
  (:require [clojure.walk :as walk]
            [ajax.core :refer [GET]]
            [help.core :as help]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.frp :as frp]))

(def term
  (frp/event))

(def response
  (frp/event))

(def users
  (->> response
       (help/<$> :items)
       (frp/stepper [])))

(defn http-search-github-component
  [users*]
  [:div
   [:label "Search:"]
   [:input {:on-change (fn [event*]
                         (-> event*
                             .-target.value
                             term))
            :type      "text"}]
   (->> users*
        (map (fn [user*]
               [:li
                [:a {:href (:html_url user*)}
                 (:name user*)]]))
        (cons :ul)
        vec)])

(def http-search-github
  (help/<$> http-search-github-component users))

(def endpoint
  "https://api.github.com/search/repositories")

(def option
  (->> term
       (core/remove empty?)
       (help/<$> (partial assoc-in
                          {:handler (comp response
                                          walk/keywordize-keys)}
                          [:params :q]))))

(frp/on (partial GET endpoint) option)
