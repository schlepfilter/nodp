(ns nodp.jdp.asynchronous-method-invocation
  (:require [nodp.helpers :as helpers]))

(defn- get-thread-name
  []
  (-> (Thread/currentThread)
      .getName))

(def prefix-with-thread-name
  (partial str "[" (get-thread-name) "] - "))

(def get-completion
  (partial str "Task completed with: "))

(def get-prefixed-completion
  (comp prefix-with-thread-name get-completion))

(defn get-value
  [{:keys [value delay callback-prefix]}]
  (future
    (Thread/sleep delay)
    (println (get-prefixed-completion value))
    (if callback-prefix
      (println (get-prefixed-completion callback-prefix ": " value)))
    value))

(defn- get-result
  [n m]
  (str "Result " n ": " @(get-value m)))

(def get-prefixed-result
  (comp prefix-with-thread-name get-result))

(def get-results
  (partial map get-prefixed-result (range)))

(helpers/printall (get-results [{:value 10
                                 :delay 500}
                                {:value "test"
                                 :delay 300}]))
