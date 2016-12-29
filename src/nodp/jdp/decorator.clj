(ns nodp.jdp.decorator
  (:require [clojure.string :as str]
            [plumbing.map :as map]))

(def troll
  {:attack "The troll swings at you with a club!"
   :flee   "The troll shrieks in horror and runs away!"
   :kind   "simple"
   :power  10})

(defn- get-troll
  [{:keys [kind power]}]
  (str kind " troll power " power))

(def describe
  (comp (partial str/join "\n")
        (juxt :attack
              :flee
              get-troll)))

(describe troll)

(def smart
  {:attack "It throws a rock at you!"
   :flee   "It calls for help!"
   :kind   "smart"
   :power  20})

(defmulti decorate* (comp first
                          vector))

(defmacro defmethods
  [multifn dispatch-vals & fn-tail]
  `(run! (fn [dispatch-val#]
           (defmethod ~multifn dispatch-val# ~@fn-tail))
         ~dispatch-vals))

(defmethods decorate* [:attack :flee]
            [_ & more]
            (str/join "\n" more))

(defmethod decorate* :kind
  [_ _ kind]
  kind)

(defmethod decorate* :power
  [_ & more]
  (apply + more))

(def decorate
  (partial map/merge-with-key decorate*))

(describe (decorate troll smart))
