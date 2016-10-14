(ns gof.jdpe.bridge)

(defn- start
  [engine]
  (-> engine
      (update :running (constantly true))
      (update :action conj "Engine started")))

(defn- increase-power
  [engine]
  (-> engine
      (update :power inc)
      (update :action conj "Engine power increased to ")))

(def turn-on
  start)

(def accelerate
  increase-power)

(def get-engine
  (constantly {:action  []
               :power   0
               :running false}))

((comp accelerate turn-on get-engine))
