(ns nodp.jdp.decorator
  (:require [clojure.string :as str]
            [plumbing.map :as map]
            [nodp.helpers :as helpers]))

(def troll
  {:attack "The troll swings at you with a club!"
   :flee   "The troll shrieks in horror and runs away!"
   :kind   "simple"
   :power  10})

(def get-troll
  (comp str/capitalize
        helpers/space-join
        (juxt :kind
              (constantly "troll power")
              :power)))

(def line-break-join
  (partial str/join "\n"))

(def describe
  (comp line-break-join
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

(defmacro defpfmethods
  [multifn dispatch-vals f]
  `(run! (fn [dispatch-val#]
           (helpers/defpfmethod ~multifn dispatch-val# ~f))
         ~dispatch-vals))

(defpfmethods decorate* [:attack :flee]
              (comp line-break-join
                    (partial drop 1)
                    vector))

(helpers/defpfmethod decorate* :kind
                     (comp last
                           vector))

(helpers/defpfmethod decorate* :power
                     (comp (partial apply +)
                           (partial drop 1)
                           vector))

(def decorate
  (partial map/merge-with-key decorate*))

(describe (decorate troll smart))
