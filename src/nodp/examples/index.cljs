(ns ^:figwheel-always nodp.examples.index
  (:require [bidi.bidi :as bidi]
            [clojure.string :as str]
            [nodp.examples.drag-n-drop :as drag-n-drop]
            [nodp.examples.intro :as intro]
            [nodp.examples.letter-count :as letter-count]
            [nodp.examples.simple-data-binding :as simple-data-binding]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]))

(def route-function
  {:drag-n-drop         drag-n-drop/drag-n-drop
   :intro               intro/intro
   :letter-count        letter-count/letter-count
   :simple-data-binding simple-data-binding/simple-data-binding})

(def route-keywords
  (keys route-function))

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

(def index
  (frp/behavior index-component))
