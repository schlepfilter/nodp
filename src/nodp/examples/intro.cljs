(ns ^:figwheel-always nodp.examples.intro
  (:require [clojure.walk :as walk]
            [ajax.core :refer [GET POST]]
            [nodp.helpers.frp :as frp]))

(def response
  (frp/event))

(def intro
  (frp/behavior
    [:div
     [:h2 "Who to follow"]
     [:a {:href     "#"
          :on-click (fn [event*]
                      (.preventDefault event*)
                      (GET (str "https://api.github.com/users?since=" 0)
                           {:handler (comp response
                                           walk/keywordize-keys)}))}
      "refresh"]]))
