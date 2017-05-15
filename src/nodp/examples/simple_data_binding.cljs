(ns nodp.examples.simple-data-binding
  (:require [nodp.helpers :as helpers :include-macros true]))

(helpers/defcurried simple-data-binding-component
                    [{:keys [first-name last-name]} full-name]
                    [:div
                     [:h1 "Simple Data Binding Example"]
                     [:p "Show simple concepts of data binding!"]
                     [:label "First Name"]
                     [:input {:on-change   (fn [event*]
                                             (-> event*
                                                 .-target.value
                                                 first-name))
                              :placeholder "Enter First Name..."}]
                     [:div "Full Name"]
                     [:div full-name]])
