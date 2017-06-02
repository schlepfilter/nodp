(ns nodp.examples.index
  (:require [bidi.bidi :as bidi]
            [clojure.string :as str]
            [com.rpl.specter :as s]
            [nodp.examples.cycle.bmi-naive :as bmi-naive]
            [nodp.examples.cycle.checkbox :as checkbox]
            [nodp.examples.cycle.counter :as counter]
            [nodp.examples.cycle.http-search-github :as http-search-github]
            [nodp.examples.intro :as intro]
            [nodp.examples.rx.drag-n-drop :as drag-n-drop]
            [nodp.examples.rx.letter-count :as letter-count]
            [nodp.examples.rx.simple-data-binding :as simple-data-binding]
            [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.history :as history]))

(def route-function
  {:bmi-naive           bmi-naive/bmi-naive
   :checkbox            checkbox/checkbox
   :counter             counter/counter
   :drag-n-drop         drag-n-drop/drag-n-drop
   :http-search-github  http-search-github/http-search-github
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
       (mapv (comp example-component
                   (partial bidi/path-for route)))
       (s/setval s/BEGINNING [:ul])))

(def index
  (frp/behavior index-component))
