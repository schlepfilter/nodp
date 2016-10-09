(ns hfdp.factory)

(defmulti get-regional-ingredient :region)

(defmethod get-regional-ingredient :ny
  [_]
  {:cheese    "Reggiano Cheese"
   :dough     "Thin Crust Dough"
   :clams     "Fresh Clams from Long Island Sound"
   :pepperoni "Sliced Pepperoni"                            ;
   :sauce     "Marinara Sauce"
   :vegies    #{"Garlic" "Onion" "Mashroom" "Red Pepper"}})

(defmulti get-kind-ingredients :kind)

(defmethod get-kind-ingredients :cheese
  [_]
  #{:dough :sauce :cheese})

(defmacro functionize
  [macro]
  `(fn [& args#]
     (eval (cons '~macro args#))))

(defmacro apply-macro
  [macro & args]
  `(apply (functionize ~macro) ~@args))

(defmacro build
  [operator & fs]
  `(comp
     (fn [x#]
       (apply-macro ~operator x#))
     (juxt ~@fs)))

(def get-pizza
  (build select-keys get-regional-ingredient get-kind-ingredients))

(defn- make-log
  [message]
  (fn [x]
    (println message)
    x))

(defmacro defoperation
  [s]
  (let [function-name (symbol s)]
    `(def ~function-name
      (make-log ~s))))

(defmacro map-macro
  [macro & args]
  `(map (functionize ~macro) ~@args))

(defmacro defall
  [expr]
  `(def _# (doall ~expr)))

(defall (map-macro defoperation ["box" "cut" "bake" "prepare"]))

(def transform
  (comp box cut bake prepare))

(def order
  (comp transform get-pizza))

(order {:region :ny
        :kind   :cheese})
