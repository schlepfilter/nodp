(ns nodp.examples.cycle.autocomplete-search
  (:require [ajax.core :refer [GET]]
            [com.rpl.specter :as s]
            [help.core :as help]
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
  (help/<> (help/<$> (constantly true) response)
           (help/<$> (constantly false) enter)))

(def relative-number
  (->> (help/<> (help/<$> (constantly inc)
                          (core/filter (partial = "ArrowDown") key-down))
                (help/<$> (constantly dec)
                          (core/filter (partial = "ArrowUp") key-down))
                (help/<$> (constantly (constantly 0)) response))
       (frp/accum 0)))

(def suggestions
  (->> (help/<$> second response)
       (frp/stepper [])))

(def valid-number
  ((help/lift-a (fn [relative-number* total-number]
                  (if (zero? total-number)
                    0
                    (mod relative-number* total-number))))
    (frp/stepper 0 relative-number)
    (help/<$> count suggestions)))

(def completion
  (->> valid-number ((help/lift-a nth) suggestions)
       (frp/snapshot enter)
       (help/<$> second)))

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

(def label-style
  {:display    "inline-block"
   :text-align "right"
   :width      100})

(def section-style
  {:margin-bottom 10})

(defn autocomplete-search-component
  ;TODO display suggestions
  [query-input* suggestion-list*]
  [:div {:style {:background "hsl(0, 0%, 94%)"
                 :padding    5}}
   [:section {:style section-style}
    [:label {:style label-style}
     "Query:"]
    [:div {:style {:display  "inline-block"
                   :position "relative"
                   :width    300}}
     query-input*
     suggestion-list*]]
   [:section {:style section-style}
    [:label {:style label-style}
     "Some field:"]
    [:input {:type "text"}]]])

(def query
  (->> (help/<> typing completion)
       (frp/stepper "")))

(def query-input
  (help/<$> query-input-component query))

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
                                             :list-style    "none"
                                             :padding       "3px 0px 3px 8px"}}
                       x]))
       ((fn [lis]
          (if (empty? lis)
            []
            (s/transform (s/srange number* (inc number*))
                         (fn [[[_ m s]]]
                           [[:li (s/setval [:style :background] green m)
                             s]])
                         lis))))
       (concat [:ul
                {:style    {:background    "white"
                            :border        border
                            :border-bottom "0px"
                            :box-shadow    "0px 4px 4px hsl(0, 0%, 86.3%)"
                            :display       (if suggested*
                                             "block"
                                             "none")
                            :margin        0
                            :padding       0
                            :position      "absolute"
                            :width         "100%"}
                 :on-click #(suggested false)}])
       vec))

(def suggestion-list
  ((help/lift-a suggestion-list-component)
    (frp/stepper false suggested)
    suggestions
    valid-number))

(def autocomplete-search
  ((help/lift-a autocomplete-search-component) query-input suggestion-list))

(def endpoint
  "https://en.wikipedia.org/w/api.php")

(def option
  (->> typing
       (core/remove empty?)
       (help/<$> (partial assoc-in
                          {:handler response
                           :params  {:action "opensearch"
                                     ;https://www.mediawiki.org/wiki/Manual:CORS#Description
                                     ;For anonymous requests, origin query string parameter can be set to * which will allow requests from anywhere.
                                     :origin "*"}}
                          [:params :search]))))

(frp/on (partial GET endpoint) option)
