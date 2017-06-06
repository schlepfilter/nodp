(ns nodp.jdpe.null-object
  (:require [aid.core :as aid]
            [clojure.string :as str]
            [com.rpl.specter :as s]
            [cuerdas.core :as cuerdas]
            [nodp.helpers :as helpers]))

(defn- make-get-turn-action
  [on]
  (comp (partial (aid/flip str) (->> (if on
                                        "ON"
                                        "OFF")
                                     (str " light ")))
        str/capitalize
        cuerdas/human))

(defn- make-turn
  [on]
  (fn [{kind :kind :as environment}]
    (case kind
      :null environment
      (->> environment
           (s/setval :on on)
           ((helpers/make-add-action (comp (make-get-turn-action on)
                                           :kind)))))))

(def check
  (helpers/make-add-action :on))

(def turn-on
  (make-turn true))

(def turn-off
  (make-turn false))

(def inspect
  (comp :actions
        check
        turn-off
        turn-on))

(def get-environment
  (partial assoc {:actions []
                  :on      false}
           :kind))

(mapcat (comp inspect
              get-environment)
        [:oil-level :brake-fluid :null])
