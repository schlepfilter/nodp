(ns hfdp.template-method)

(def boil
  (partial println "Boiling water"))

(defmulti brew identity)

(defmethod brew :coffee
  [_]
  (println "Dripping Coffee through filter"))

(defn prepare
  [kind]
  (boil)
  (brew kind)
  ;(pour)
  ;(add-condiments kind)
  )

(prepare :coffee)