(ns nodp.examples.cycle.autocomplete-search
  (:require [clojure.walk :as walk]
            [ajax.core :refer [GET]]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.clojure.core :as core]))

(def typing
  (frp/event))

(def response
  (frp/event))

(defn query-input-component
  [query*]
  [:input {:on-change (fn [event*]
                        (-> event*
                            .-target.value
                            typing))
           :type      "text"
           :value     query*}])

(defn autocomplete-search-component
  ;TODO display suggestions
  [query-input*]
  [:div
   [:section
    [:label "Query:"]
    query-input*]
   [:section
    [:label "Some field:"]
    [:input {:type "text"}]]])

(def query
  (frp/stepper "" typing))

(def query-input
  (helpers/<$> query-input-component query))

(def autocomplete-search
  ;TODO display suggestions
  ((helpers/lift-a autocomplete-search-component) query-input))

(def endpoint
  "https://en.wikipedia.org/w/api.php")

(def option
  (->> typing
       (core/remove empty?)
       (helpers/<$> (partial assoc-in
                             {:handler (comp response
                                             walk/keywordize-keys)
                              :params  {:action "opensearch"
                                        ;https://www.mediawiki.org/wiki/Manual:CORS#Description
                                        ;For anonymous requests, origin query string parameter can be set to * which will allow requests from anywhere.
                                        :origin "*"}}
                             [:params :search]))))

(frp/on (partial GET endpoint) option)
