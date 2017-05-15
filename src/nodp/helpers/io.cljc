;primitives.event and io namespaces are separated to limit the impact of :refer-clojure :exclude for transduce
(ns nodp.helpers.io
  (:require [clojure.string :as str]
            [cats.context :as ctx]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple])
  #?(:cljs (:require-macros [nodp.helpers.io :refer [defcurriedmethod]])))

(defn event
  []
  (->> (nodp.helpers/mempty)
       (ctx/with-context event/context)))

(def get-keyword
  (comp keyword
        str/lower-case
        last
        (partial (helpers/flip str/split) #?(:clj  #"\."
                                             :cljs #"/"))
        ;JavaScript doesn't seem to implement lookbehind.
        ;=> (partial re-find #"(?<=\.)\w*$")
        ;#object[SyntaxError SyntaxError: Invalid regular expression: /(?<=\.)\w*$/: Invalid group]
        pr-str
        type))

(defmulti get-effect! (comp get-keyword
                            second
                            vector))

#?(:clj (defmacro defcurriedmethod
          [multifn dispatch-val bindings & body]
          `(helpers/defpfmethod ~multifn ~dispatch-val
                                (helpers/curry ~(count bindings)
                                               (fn ~bindings
                                                 ~@body)))))

(defcurriedmethod get-effect! :event
                  [f! e network]
                  (run! (comp f!
                              tuple/snd)
                        (event/get-latests (:id e) network)))

(defmethod get-effect! :behavior
  [f! b]
  (let [past-latest-maybe-state (atom helpers/nothing)]
    (fn [network]
      (helpers/if-then-else (partial not= @past-latest-maybe-state)
                            (juxt (partial reset! past-latest-maybe-state)
                                  (comp f!
                                        deref))
                            (maybe/just ((behavior/get-function b
                                                                network)
                                          (:time network)))))))

(def on
  (comp (partial swap! event/network-state)
        ((helpers/curry 3 s/setval*) [:effects s/END])
        vector
        get-effect!))

(def redef-events
  (partial run! (fn [from]
                  (behavior/redef from
                                  (event)))))
