(ns nodp.jdpe.null-object
  (:require [clojure.string :as str]
            [com.rpl.specter :as specter]
            [cuerdas.core :as cuerdas]
            [nodp.helpers :as helpers]))

(defn- make-get-turn-action
  [on]
  (comp (partial (helpers/flip str) (->> (if on
                                           "ON"
                                           "OFF")
                                         (str " light ")))
        str/capitalize
        cuerdas/human))

(defn make-add-action
  [f]
  (helpers/build (partial specter/transform* :actions)
                 (comp (helpers/flip (helpers/curry 2 conj))
                       f)
                 identity))

(defn- make-turn
  [on]
  (fn [{kind :kind :as environment}]
    (if (= kind :null)
      environment
      (->> environment
           (specter/setval :on on)
           ((make-add-action (comp (make-get-turn-action on)
                                   :kind)))))))

(def check
  (make-add-action :on))

(def turn-on
  (make-turn true))

(def turn-off
  (make-turn false))

(def inspect
  (comp :actions check turn-off turn-on))

(defn- get-environment
  [kind]
  {:actions []
   :kind    kind
   :on      false})

(mapcat (comp inspect get-environment) [:oil-level
                                        :brake-fluid
                                        :null])
