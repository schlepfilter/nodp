(ns nodp.jdp.asynchronous-method-invocation
  (:require [clojure.string :as str]
            [nodp.helpers :as helpers]))

(defn- get-thread-prefix
  []
  (str "[" (helpers/get-thread-name) "] - "))

(defn- prefix-with-thread-name
  [& more]
  (apply str (get-thread-prefix) more))

(def get-completion
  (partial str "Task completed with: "))

(def get-prefixed-completion
  (comp prefix-with-thread-name
        get-completion))

(defn- get-value
  [{:keys [value delay callback-prefix]}]
  (Thread/sleep delay)
  (-> value
      get-prefixed-completion
      println)
  (if callback-prefix
    (-> (prefix-with-thread-name callback-prefix ": " value)
        println)
    value))

(def get-result
  (comp str/join
        (partial interleave ["Result " ": "])
        vector))

(def get-prefixed-result
  (comp prefix-with-thread-name
        get-result))

(def get-results
  (->> (range)
       (partial map get-prefixed-result)))

(def printall
  (comp helpers/printall
        get-results
        doall
        (partial take 3)
        (partial pmap get-value)))

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
