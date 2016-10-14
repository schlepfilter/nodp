(ns gof.hfdp.template-method)

(defn- defmulti-identity
  [mm-name]
  (-> `(defmulti ~mm-name identity)
      eval))

(def defmultis-identity
  (comp dorun
        (partial map defmulti-identity)))

(defmultis-identity #{'brew 'add-condiments})

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

(defmethods :tea
            {brew           "Steeping the tea"
             add-condiments "Adding Lemon"})

(defn prepare
  [kind]
  (println "Boiling water")
  (brew kind)
  (println "Pouring into cup")
  (add-condiments kind))

(prepare :coffee)

(prepare :tea)
