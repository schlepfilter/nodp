(ns nodp.examples.cycle.autocomplete-search
  (:require [ajax.core :refer [GET]]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.clojure.core :as core]))

(def typing
  (frp/event))

(def response
  (frp/event))

(def key-down
  (frp/event))

(def relative-number
  (->> (helpers/<> (helpers/<$> (constantly inc)
                                (core/filter (partial = "ArrowDown")
                                             key-down))
                   (helpers/<$> (constantly dec)
                                (core/filter (partial = "ArrowUp")
                                             key-down))
                   (helpers/<$> (constantly (constantly 0)) response))
       (frp/accum 0)
       (frp/stepper 0)))

(def suggestions
  (frp/stepper [] (helpers/<$> second response)))

(def valid-number
  ((helpers/lift-a (fn [relative-number* total-number]
                     (if (zero? total-number)
                       0
                       (mod relative-number* total-number))))
    relative-number
    (helpers/<$> count suggestions)))

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
  [query-input* suggestion-list*]
  [:div
   [:section
    [:label "Query:"]
    query-input*
    suggestion-list*]
   [:section
    [:label "Some field:"]
    [:input {:type "text"}]]])

(def query
  (frp/stepper "" typing))

(def query-input
  (helpers/<$> query-input-component query))

(def green
  "hsl(120, 100%, 50%)")

(defn suggestion-list-component
  ;TODO don't highlight a suggestion after response occurs
  [suggestions* number*]
  (->> suggestions*
       (map (partial vector :li))
       ((fn [lis]
          (if (empty? lis)
            []
            (s/transform (s/srange number* (inc number*))
                         (fn [[[_ s]]]
                           [[:li {:style {:background-color green}} s]])
                         lis))))
       (concat [:ul {:display "inline-block"}])
       vec))

(def suggestion-list
  ((helpers/lift-a suggestion-list-component) suggestions valid-number))

(def autocomplete-search
  ;TODO display suggestions
  ((helpers/lift-a autocomplete-search-component) query-input suggestion-list))

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
