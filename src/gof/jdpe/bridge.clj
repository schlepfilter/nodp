(ns gof.jdpe.bridge)

(defn- start
  [engine]
  (-> engine
      (update :running (constantly true))
      (update :action conj "Engine started")))

(defn- add-power-action
  [engine]
  (->> (:power engine)
       (str "Engine power increased to ")
       (update engine :action conj)))

(defn- increase-power
  [engine]
  (-> engine
      (update :power inc)
      add-power-action))

(defn- decrease-power
  [engine]
  (-> engine
      (update :power dec)
      add-power-action))

(def turn-on
  start)

(def accelerate
  increase-power)

(def break
  decrease-power)

(def get-engine
  (constantly {:action  []
               :power   0
               :running false}))

((comp break accelerate turn-on get-engine))
