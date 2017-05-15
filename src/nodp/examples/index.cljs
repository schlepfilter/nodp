(ns nodp.examples.index
  (:require [bidi.bidi :as bidi]
            [clojure.string :as str]
            [nodp.helpers :as helpers]
            [nodp.helpers.history :as history]))

(def route-keywords
  [:letter-count :simple-data-binding])

(defn unkebab
  [s]
  (str/replace s #"-" ""))

(def example-route
  (zipmap (map (comp unkebab
                     (partial (helpers/flip subs) 1)
                     str)
               route-keywords)
          route-keywords))

(def route
  ["/" (merge {"" :index}
              example-route)])

(defn example-component
  [path]
  [:a {:href     path
       :on-click (fn [event*]
                   (.preventDefault event*)
                   (history/push-state {} {} path))}
   [:li (subs path 1)]])

(def index-component
  (->> route-keywords
       (map (comp example-component
                  (partial bidi/path-for route)))
       (cons :ul)
       (into [])))
