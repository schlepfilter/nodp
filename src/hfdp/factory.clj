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
     (-> (cons '~macro args#)
         eval)))

(defmacro build
  [operator & fs]
  `(comp
     (partial apply (functionize ~operator))
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

(defmacro defall
  [expr]
  `(def _# (doall ~expr)))

(-> (functionize defoperation)
    (map ["box" "cut" "bake" "prepare"])
    defall)

(def transform
  (comp box cut bake prepare))

(def order
  (comp transform get-pizza))

(order {:region :ny
        :kind   :cheese})
