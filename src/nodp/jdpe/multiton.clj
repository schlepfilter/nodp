(ns nodp.jdpe.multiton)

(defn- get-generator
  []
  (atom 0))

(def generator
  {:engine  (get-generator)
   :vehicle (get-generator)})

(defn- get-next-serial
  [k]
  (swap! (generator k) inc))

;This definition is less readable.
;(def get-next-serial
;  (comp (partial (helpers/flip swap!) inc)
;        generator))

(def print-next-serial
  (comp println
        get-next-serial))

(dotimes [_ 3]
  (print-next-serial :engine))

(dotimes [_ 3]
  (print-next-serial :vehicle))
