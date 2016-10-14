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

(defn- change-power
  [engine change]
  (-> engine
      (update :power change)
      add-power-action))

(defn- increase-power
  [engine]
  (if (and (:running engine) (< (:power engine) 10))
    (change-power engine inc)
    engine))

(defn- decrease-power
  [engine]
  (change-power engine dec))

(def turn-on
  start)

(def accelerate
  increase-power)

(def break
  decrease-power)

(def engine
  {:action  []
   :power   0
   :running false})

((comp break accelerate turn-on) engine)
