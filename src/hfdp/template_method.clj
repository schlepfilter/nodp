(ns hfdp.template-method)

(def boil
  (partial println "Boiling water"))

(defmulti brew identity)

(def pour
  (partial println "Pouring into cup"))

(defmulti add-condiments identity)

(defmacro defall
  [expr]
  `(def _# (doall ~expr)))

(defmacro make-defmethod
  [dispatch-val]
  `(fn [[k# v#]]
     (let [multifn# (symbol (name k#))]
       (defmethod (eval multifn#) ~dispatch-val
         [_#]
         (println v#)))))

(defmacro defmethods
  [dispatch-val f-m]
  `(defall (map (make-defmethod ~dispatch-val) ~f-m)))

(defmethods :coffee
            {:brew           "Dripping Coffee through filter"
             :add-condiments "Adding Sugar and Milk"})

(defn prepare
  [kind]
  (boil)
  (brew kind)
  (pour)
  (add-condiments kind))

(prepare :coffee)