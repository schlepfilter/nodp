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

(def key-down
  (frp/event))

(def number
  (frp/accum 0
             (helpers/<> (helpers/<$> (constantly inc)
                                      (core/filter (partial = "ArrowDown")
                                                   key-down))
                         (helpers/<$> (constantly dec)
                                      (core/filter (partial = "ArrowUp")
                                                   key-down))
                         (helpers/<$> (constantly (constantly 0)) response))))

(defn query-input-component
  [query*]
  [:input {:on-change   (fn [event*]
                          (-> event*
                              .-target.value
                              typing))
           :on-key-down (fn [event*]
                          (-> event*
                              .-key
                              key-down))
           :type        "text"
           :value       query*}])

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

(def suggestions
  (frp/stepper [] (helpers/<$> second response)))

(def autocomplete-search
  ;TODO display suggestions
  ((helpers/lift-a autocomplete-search-component) query-input))

(def endpoint
  "https://en.wikipedia.org/w/api.php")

(def option
  (->> typing
       (core/remove empty?)
       (helpers/<$> (partial assoc-in
                             {:handler response
                              :params  {:action "opensearch"
                                        ;https://www.mediawiki.org/wiki/Manual:CORS#Description
                                        ;For anonymous requests, origin query string parameter can be set to * which will allow requests from anywhere.
                                        :origin "*"}}
                             [:params :search]))))

(frp/on (partial GET endpoint) option)
