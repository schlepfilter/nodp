(ns ^:figwheel-always nodp.examples.intro
  (:require [clojure.walk :as walk]
            [ajax.core :refer [GET POST]]
            [com.rpl.specter :as s]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.unit :as unit]))

(def suggestion-number
  3)

(def response
  (frp/event))

(def clicks
  (repeatedly suggestion-number frp/event))

(def click-counts
  (map (comp (partial frp/stepper 0)
             core/count)
       clicks))

(def click-components
  (map (fn [click]
         [:li [:a {:href     "#"
                   :on-click (fn [event*]
                               (.preventDefault event*)
                               (click unit/unit))}
               "x"]])
       clicks))

(def intro
  (frp/behavior
    (s/setval
      s/END
      click-components
      [:div
       [:h2 "Who to follow"]
       [:a {:href     "#"
            :on-click (fn [event*]
                        (.preventDefault event*)
                        (GET (str "https://api.github.com/users?since=" 0)
                             {:handler (comp response
                                             walk/keywordize-keys)}))}
        "refresh"]])))
