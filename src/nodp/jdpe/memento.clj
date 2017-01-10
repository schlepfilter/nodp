(ns nodp.jdpe.memento
  (:require [clojure.set :as set]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]))

(def environment
  {:actions []})

(def set-before
  (partial (helpers/flip set/rename-keys) {:now :before}))

(def make-set-now*
  ((helpers/curry s/setval*) :now))

(defn- make-set-now
  [n]
  (comp (make-set-now* n)
        set-before))

(defn- make-get
  [{:keys [key prefix]}]
  (partial helpers/transfer* [:actions s/END] (comp vector
                                                    (partial str prefix)
                                                    key)))

(def get-now
  (make-get {:key    :now
             :prefix "Current speed: "}))

(def get-before
  (make-get {:key    :before
             :prefix "Previous speed: "}))

(def get-now-before
  (comp get-before
        get-now))

(def get-memento
  (partial (helpers/flip select-keys) [:now :before]))

(def save
  (partial helpers/transfer* :memento get-memento))

(defn- make-single-restore
  [k]
  (partial helpers/transfer* k (comp k
                                     :memento)))

(def restore
  (apply comp (map make-single-restore #{:now :before})))

(defn- get-actions
  [& commands]
  (-> environment
      ((apply comp commands))
      :actions))

(get-actions get-now-before
             restore
             get-now-before
             (make-set-now 80)
             save
             get-now-before
             (make-set-now 100)
             (make-set-now 50))
