(ns nodp.jdpe.chain-of-responsibility
  (:require [cats.builtin]
            [cats.core :as m]
            [cats.monad.either :as either]
            [nodp.helpers :as helpers]))

(defn- make-have-word?
  [words]
  (fn [email]
    (some (partial (helpers/flip re-find) email) (map re-pattern words))))

;This definition is harder to read.
;(defn- make-have-word?
;  [words]
;  (comp ((helpers/flip (helpers/curry some)) (map re-pattern words))
;        (helpers/flip (helpers/curry re-find 2))))

(defn- make-handle
  [{:keys [words action]}]
  (fn [email]
    (helpers/casep email
                   (make-have-word? words) (either/left action)
                   (either/right email))))

(defmacro defhandle
  [f-name m]
  `(def ~f-name
     (make-handle ~m)))

(defhandle handle-spam {:words  #{"viagra" "pills" "medicine"}
                        :action "This is a spam email."})

(defhandle handle-sales {:words  #{"buy" "purchase"}
                         :action "Email handled by sales department."})

(defhandle handle-service {:words  #{"service" "repair"}
                           :action "Email handled by service department."})

(defhandle handle-management {:words  #{"complain" "bad"}
                              :action "Email handled by manager."})

(defn- handle-general
  [_]
  (either/left "Email handled by general enquires."))

(defn- comp->>=
  [& fs]
  (->> fs
       (map (helpers/flip (helpers/curry m/>>=)))
       (apply comp)))

(def handle
  (comp (comp->>= handle-general
                  handle-management
                  handle-service
                  handle-sales
                  handle-spam)
        either/right))

(handle "I need my car repaired")
