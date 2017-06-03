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

(def enter
  (core/filter (partial = "Enter")
               key-down))

(def suggested
  (helpers/<> (helpers/<$> (constantly true) response)
              (helpers/<$> (constantly false)
                           enter)))

(def relative-number
  (->> (helpers/<> (helpers/<$> (constantly inc)
                                (core/filter (partial = "ArrowDown")
                                             key-down))
                   (helpers/<$> (constantly dec)
                                (core/filter (partial = "ArrowUp")
                                             key-down))
                   (helpers/<$> (constantly (constantly 0)) response))
       (frp/accum 0)))

(def suggestions
  (frp/stepper [] (helpers/<$> second response)))

(def valid-number
  ((helpers/lift-a (fn [relative-number* total-number]
                     (if (zero? total-number)
                       0
                       (mod relative-number* total-number))))
    (frp/stepper 0 relative-number)
    (helpers/<$> count suggestions)))

(def completion
  (->> valid-number ((helpers/lift-a nth) suggestions)
       (frp/snapshot enter)
       (helpers/<$> second)))

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
  (frp/stepper "" (helpers/<> typing completion)))

(def query-input
  (helpers/<$> query-input-component query))

(def green
  "hsl(145, 66%, 74%)")

(defn suggestion-list-component
  ;TODO don't highlight a suggestion after response occurs
  [suggested* suggestions* number*]
  (->> suggestions*
       (map-indexed (fn [index x]
                      [:li {:on-mouse-enter #(relative-number index)
                            :on-click       #(completion x)
                            :style          {:list-style "none"}}
                       x]))
       ((fn [lis]
          (if (empty? lis)
            []
            (s/transform (s/srange number* (inc number*))
                         (fn [[[_ m s]]]
                           [[:li (s/setval [:style :background-color] green m)
                             s]])
                         lis))))
       (concat [:ul {:style    {:display (if suggested*
                                           "block"
                                           "none")}
                     :on-click #(suggested false)}])
       vec))

(def suggestion-list
  ((helpers/lift-a suggestion-list-component)
    (frp/stepper false suggested)
    suggestions
    valid-number))

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

