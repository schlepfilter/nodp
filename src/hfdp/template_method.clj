(ns hfdp.template-method)

(def boil
  (partial println "Boiling water"))

(defn- defmulti-identity
  [mm-name]
  (eval `(defmulti ~mm-name identity)))

(defn- defmultis-identity
  [mm-names]
  (-> (map defmulti-identity mm-names)
      dorun))

(defmultis-identity ['brew 'add-condiments])

(def pour
  (partial println "Pouring into cup"))

(defn- make-defmethod
  [dispatch-val]
  (fn [[k v]]
    (defmethod k dispatch-val
      [_]
      (println v))))

(defn- defmethods
  [dispatch-val f-m]
  (-> (make-defmethod dispatch-val)
      (map f-m)
      dorun))

(defmethods :coffee
            {brew           "Dripping Coffee through filter"
             add-condiments "Adding Sugar and Milk"})

(defn prepare
  [kind]
  (boil)
  (brew kind)
  (pour)
  (add-condiments kind))

(prepare :coffee)