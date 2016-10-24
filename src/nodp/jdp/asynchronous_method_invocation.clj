(ns nodp.jdp.asynchronous-method-invocation)

(defn- get-thread-name
  []
  (-> (Thread/currentThread)
      .getName))

(def prefix-with-thread-name
  (partial str "[" (get-thread-name) "] - "))

(def log
  (comp println prefix-with-thread-name))

(defn get-value
  [{:keys [value delay callback-prefix]}]
  (future
    (Thread/sleep delay)
    (log (str "Task completed with: " value))
    (if callback-prefix
      (log (str callback-prefix ": " value)))
    value))

(log (str "Result 1: " @(get-value {:value 10
                                    :delay 500})))
