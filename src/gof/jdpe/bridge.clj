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

(defn- get-change-power
  [change]
  (fn [engine]
    (-> engine
        (update :power change)
        add-power-action)))

(def increase-power
  (get-change-power inc))

(def decrease-power
  (get-change-power dec))

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
