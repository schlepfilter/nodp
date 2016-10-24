(ns nodp.jdp.asynchronous-method-invocation
  (:require [nodp.helpers :as helpers]
            [clojure.string :as str]))

(defn- get-thread-name
  []
  (-> (Thread/currentThread)
      .getName))

(defn- get-thread-prefix
  []
  (str "[" (get-thread-name) "] - "))

(defn- prefix-with-thread-name
  [& more]
  (apply str (get-thread-prefix) more))

(def get-completion
  (partial str "Task completed with: "))

(def get-prefixed-completion
  (comp prefix-with-thread-name get-completion))

(defn- get-future
  [{:keys [value delay callback-prefix]}]
  (future
    (Thread/sleep delay)
    (-> value
        get-prefixed-completion
        println)
    (if callback-prefix
      (-> (prefix-with-thread-name callback-prefix ": " value)
          println))
    value))

(defn- get-result
  [n value]
  (str "Result " n ": " value))

(def get-prefixed-result
  (comp prefix-with-thread-name get-result))

(def get-results
  (->> (range)
       (partial map get-prefixed-result)))

(def printall
  (comp helpers/printall
        get-results
        doall
        (partial map deref)
        (partial take 3)
        (partial map get-future)))

(printall [{:value 10
            :delay 500}
           {:value "test"
            :delay 300}
           {:value 50
            :delay 700}
           {:value           20
            :delay           400
            :callback-prefix "Callback result 4"}
           {:value           "callback"
            :delay           600
            :callback-prefix "Callback result 5"}])
