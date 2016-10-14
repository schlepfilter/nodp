(ns gof.jdpe.bridge)

(defn- start
  [engine]
  (-> engine
      (update :running (constantly true))
      (update :action conj "Engine started")))

(defn- add-increase-power-action
  [engine]
  (->> (:power engine)
       (str "Engine power increased to ")
       (update engine :action conj)))

(defn- increase-power
  [engine]
  (-> engine
      (update :power inc)
      add-increase-power-action))

(def turn-on
  start)

(def accelerate
  increase-power)

(def get-engine
  (constantly {:action  []
               :power   0
               :running false}))

((comp accelerate turn-on get-engine))
