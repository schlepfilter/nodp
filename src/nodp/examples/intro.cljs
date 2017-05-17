(ns nodp.examples.intro
  (:require [clojure.walk :as walk]
            [ajax.core :refer [GET POST]]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
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

(def offset-counts
  (map (fn [click-count offset]
         (helpers/<$> (partial + offset) click-count))
       click-counts
       (range 0 30 10)))

(def users
  (apply (helpers/lift-a (fn [response* & click-counts]
                           (map (partial nth (cycle response*))
                                click-counts)))
         (frp/stepper (repeat {}) response) offset-counts))

(defn get-user-component
  [user* click]
  [:li {:style {:visibility (helpers/casep user*
                                           empty? "hidden"
                                           "visible")}}
   [:img {:src (:avatar_url user*)}]
   [:a {:href (:html_url user*)} (:login user*)]
   [:a {:href     "#"
        :on-click (fn [event*]
                    (.preventDefault event*)
                    (click unit/unit))}
    "x"]])

(defn intro-component
  [users*]
  (s/setval s/END
            (map get-user-component
                 users*
                 clicks)
            [:div
             [:h2 "Who to follow"]
             [:a {:href     "#"
                  :on-click (fn [event*]
                              (.preventDefault event*)
                              (GET (str "https://api.github.com/users?since=" 0)
                                   {:handler (comp response
                                                   walk/keywordize-keys)}))}
              "refresh"]]))

(def intro
  (helpers/<$> intro-component users))
