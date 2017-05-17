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

(def user-number
  30)

(def offset-counts
  (->> suggestion-number
       (quot user-number)
       (range 0 user-number)
       (map (fn [click-count offset]
              (helpers/<$> (partial + offset) click-count))
            click-counts)))

(def users
  (apply (helpers/lift-a (fn [response* & click-counts]
                           (map (partial nth (cycle response*))
                                click-counts)))
         (frp/stepper (repeat {}) response) offset-counts))

(def size
  "2.5em")

(def link-style
  {:display     "inline-block"
   :margin-left "0.3em"})

(defn get-user-component
  [user* click]
  [:li {:style {:align-items "center"
                :display     "flex"
                :visibility  (helpers/casep user*
                                            empty? "hidden"
                                            "visible")}}
   [:img {:src   (:avatar_url user*)
          :style {:border-radius "1.25em"
                  :height        size
                  :width         size}}]
   [:a {:href  (:html_url user*)
        :style link-style}
    (:login user*)]
   [:a {:href     "#"
        :on-click (fn [event*]
                    (.preventDefault event*)
                    (click unit/unit))
        :style    link-style}
    "x"]])

(defn handle-click
  [event*]
  (.preventDefault event*)
  (GET (->> (js/Math.random)
            (* 500)
            int
            (str "https://api.github.com/users?since="))
       {:handler (comp response
                       walk/keywordize-keys)}))

(def grey
  "hsl(0, 0%, 93%)")

(defn intro-component
  [users*]
  (s/setval s/END
            (map get-user-component
                 users*
                 clicks)
            [:div {:style {:border (str "0.125em solid " grey)}}
             [:div {:style {:background-color grey
                            :padding          "0.3em"}}
              [:h2 {:style {:display "inline-block"}}
               "Who to follow"]
              [:a {:href     "#"
                   :on-click handle-click
                   :style    {:margin-left "1.25em"}}
               "refresh"]]]))

(def intro
  (helpers/<$> intro-component users))
