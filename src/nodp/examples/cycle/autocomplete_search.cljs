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
           :style       {:width "100%"}
           :value       query*}])

(defn autocomplete-search-component
  ;TODO display suggestions
  [query-input* suggestion-list*]
  [:div
   [:section
    [:label "Query:"]
    [:div {:style {:display  "inline-block"
                   :position "relative"
                   :width    300}}
     query-input*
     suggestion-list*]]
   [:section
    [:label "Some field:"]
    [:input {:type "text"}]]])

(def query
  (frp/stepper "" (helpers/<> typing completion)))

(def query-input
  (helpers/<$> query-input-component query))

(def green
  "hsl(145, 66%, 74%)")

(def border
  "1px solid hsl(0, 0%, 80%)")

(defn suggestion-list-component
  [suggested* suggestions* number*]
  (->> suggestions*
       (map-indexed (fn [index x]
                      [:li {:on-click       #(completion x)
                            :on-mouse-enter #(relative-number index)
                            :style          {:border-bottom border
                                             :list-style    "none"}}
                       x]))
       ((fn [lis]
          (if (empty? lis)
            []
            (s/transform (s/srange number* (inc number*))
                         (fn [[[_ m s]]]
                           [[:li (s/setval [:style :background-color] green m)
                             s]])
                         lis))))
       (concat [:ul {:style    {:background-color "white"
                                :border           border
                                :border-bottom    "0px"
                                :display          (if suggested*
                                                    "block"
                                                    "none")
                                :margin           0
                                :padding          0
                                :position         "absolute"
                                :width            "100%"}
                     :on-click #(suggested false)}])
       vec))

(def suggestion-list
  ((helpers/lift-a suggestion-list-component)
    (frp/stepper false suggested)
    suggestions
    valid-number))

(def autocomplete-search
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
