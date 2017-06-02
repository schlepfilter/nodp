(ns nodp.examples.cycle.http-search-github
  (:require [clojure.walk :as walk]
            [ajax.core :refer [GET]]
            [nodp.helpers.frp :as frp]
            [nodp.helpers :as helpers]
            [nodp.helpers.clojure.core :as core]))

(def term
  (frp/event))

(def response
  (frp/event))

(def option
  (->> term
       (core/filter (comp pos?
                          count))
       (helpers/<$> (partial assoc-in
                             {:handler (comp response
                                             walk/keywordize-keys)}
                             [:params :q]))))

(def users
  (->> response
       (helpers/<$> :items)
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
  (helpers/<$> http-search-github-component users))

(def endpoint
  "https://api.github.com/search/repositories")

(frp/on (partial GET endpoint) option)
