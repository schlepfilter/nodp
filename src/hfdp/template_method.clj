(ns hfdp.template-method)

(def boil
  (partial println "Boiling water"))

(defmulti brew identity)

(def pour
  (partial println "Pouring into cup"))

(defmulti add-condiments identity)

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