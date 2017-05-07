;primitives.event and io namespaces are separated to limit the impact of :refer-clojure :exclude for transduce
(ns nodp.helpers.io
  (:require [clojure.string :as str]
            [cats.context :as ctx]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers :as helpers])
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

(defmethod get-effect! :behavior
  [f! b]
  (let [past-latest-maybe-state (atom helpers/nothing)]
    (fn [network]
      (when (not= @past-latest-maybe-state
                  ;TODO refactor
                  (maybe/just ((behavior/get-function b network) (:time network))))
        (reset! past-latest-maybe-state
                (maybe/just ((behavior/get-function b network) (:time network))))
        (f! ((behavior/get-function b network) (:time network)))))))

(def on
  (comp (partial swap! event/network-state)
        ((m/curry s/setval*) [:effects s/END])
        vector
        get-effect!))
