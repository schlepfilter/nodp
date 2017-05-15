(ns ^:figwheel-always nodp.examples.letter-count
  (:require [nodp.helpers :as helpers :include-macros true]
            [nodp.helpers.frp :as frp]))

(helpers/defcurried letter-count-component
                    [e message]
                    [:div
                     [:h1 "Letter Counting Example"]
                     [:p "Example to show getting the current length of the input."]
                     [:div [:p
                            "Text buffer: "
                            [:input {:on-change (fn [event*]
                                                  (-> event*
                                                      .-target.value.length
                                                      e))}]]
                      [:p message]]])

(def length
  (frp/event))

(def letter-count
  (->> length
       (helpers/<$> (partial str "length: "))
       (frp/stepper "Start Typing!")
       (helpers/<$> (letter-count-component length))))
