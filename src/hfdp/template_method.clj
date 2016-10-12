(ns hfdp.template-method)

(def boil
  (partial println "Boiling water"))

(defmulti brew identity)

(defmethod brew :coffee
  [_]
  (println "Dripping Coffee through filter"))

(def pour
  (partial println "Pouring into cup"))

(defmulti add-condiments identity)

(defmethod add-condiments :coffee
  [_]
  (println "Adding Sugar and Milk"))

(defn prepare
  [kind]
  (boil)
  (brew kind)
  (pour)
  (add-condiments kind))

(prepare :coffee)