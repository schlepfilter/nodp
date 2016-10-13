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

(def eval-keyword
  (comp eval symbol name))

(defmacro make-defmethod
  [dispatch-val]
  `(fn [[k# v#]]
     (defmethod (eval-keyword k#) ~dispatch-val
       [_#]
       (println v#))))

(defmacro defmethods
  [dispatch-val f-m]
  `(defall (-> (make-defmethod ~dispatch-val)
               (map ~f-m))))

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