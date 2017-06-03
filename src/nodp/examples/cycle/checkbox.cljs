(ns nodp.examples.cycle.checkbox
  (:require [help]
            [help.unit :as unit]
            [nodp.helpers.clojure.core :as core]
            [nodp.helpers.frp :as frp]))

(def check
  (frp/event))

(def checked
  (->> check
       core/count
       (help/<$> odd?)
       (frp/stepper false)))

(defn checkbox-component
  [checked*]
  [:div
   [:input {:on-change #(check unit/unit)
            :type      "checkbox"}]
   "Toggle me"
   [:p (if checked*
         "ON"
         "off")]])

(def checkbox
  (help/<$> checkbox-component checked))
