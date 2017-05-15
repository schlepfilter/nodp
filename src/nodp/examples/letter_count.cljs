(ns nodp.examples.letter-count
  (:require [nodp.helpers :as helpers :include-macros true]))

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
