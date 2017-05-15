(ns ^:figwheel-always nodp.examples.simple-data-binding
  (:require [nodp.helpers :as helpers]
            [nodp.helpers.frp :as frp]))

(defn partial-name
  [{:keys [event label]}]
  [:div
   [:label label]
   [:input {:on-change   (fn [event*]
                           (-> event*
                               .-target.value
                               event))
            :placeholder (str "Enter " label "...")}]])

(def first-name
  (frp/event))

(def last-name
  (frp/event))

(defn simple-data-binding-component
  [full-name]
  [:div
   [:h1 "Simple Data Binding Example"]
   [:p "Show simple concepts of data binding!"]
   [partial-name {:event first-name
                  :label "First Name"}]
   [partial-name {:event last-name
                  :label "Last Name"}]
   [:div "Full Name"]
   [:div full-name]])

(def full-name
  ((helpers/lift-a 3 str)
    (frp/stepper "" first-name)
    (frp/behavior " ")
    (frp/stepper "" last-name)))

(def simple-data-binding
  (helpers/<$> simple-data-binding-component full-name))
