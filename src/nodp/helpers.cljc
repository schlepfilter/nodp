(ns nodp.helpers
  (:refer-clojure :exclude [defcurried])
  (:require [clojure.string :as str]
            [beicon.core :as rx]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [aid.core :as help]
    #?@(:clj [
            [clojurewerkz.money.amounts :as ma]
            [clojurewerkz.money.currencies :as mc]
            [potemkin]])))

(def comp-just
  (partial comp maybe/just))

#?(:clj
   (defmacro defdefs
     [macro-name macro]
     (potemkin/unify-gensyms
       `(let [qualified-macro-name## (resolve '~macro-name)
              qualified-macro## (resolve '~macro)]
          (defmacro ~macro-name
            ([])
            ([x## & more##]
              `(do (~qualified-macro## ~x##)
                   (~qualified-macro-name## ~@more##))))))))

(def space-join
  (partial str/join " "))

(def comma-join
  (partial str/join ", "))

#?(:clj
   (do (defmacro defmulti-identity
         [mm-name]
         `(defmulti ~mm-name identity))

       (defdefs defmultis-identity
                defmulti-identity)))

(defn- make-defmethod
  [dispatch-val]
  (fn [[k v]]
    (defmethod k dispatch-val
      [_]
      v)))

(defn defmethods
  [dispatch-val f-m]
  (-> (make-defmethod dispatch-val)
      (run! f-m)))

(def printall
  (partial run! println))

(def printstream
  (partial (help/flip rx/on-next) println))

#?(:clj
   (defn get-thread-name
     []
     (-> (Thread/currentThread)
         .getName)))

(defn transfer*
  [apath transfer-fn structure]
  ((help/build (partial s/setval* apath)
               transfer-fn
               identity)
    structure))

(defn make-add-action
  [f]
  (partial transfer*
           [:actions s/END]
           (comp vector
                 f)))

;This definition is less readable.
;(defn make-add-action
;  [f]
;  (build (partial s/transform* :actions)
;         (comp (flip (curry conj 2))
;               f)
;         identity))

#?(:clj
   (def get-usd
     (partial ma/amount-of mc/USD)))
