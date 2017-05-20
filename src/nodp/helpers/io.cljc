;primitives.event and io namespaces are separated to limit the impact of :refer-clojure :exclude for transduce
(ns nodp.helpers.io
  (:require [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.helpers.derived :as derived]
            [nodp.helpers.primitives.behavior :as behavior]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.protocols :as protocols]
            [nodp.helpers.tuple :as tuple])
  #?(:cljs (:require-macros [nodp.helpers.io :refer [defcurriedmethod]])))

(defmulti get-effect! (comp protocols/-get-keyword
                            second
                            vector))

;This definition of get-effect! produces the following failure in :advanced.
;Reloading Clojure file "/nodp/hfdp/observer/synchronization.clj" failed.
;clojure.lang.Compiler$CompilerException: java.lang.IllegalArgumentException: No method in multimethod 'get-effect!' for dispatch value
;(defmulti get-effect! (comp helpers/infer
;                            second
;                            vector))

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
  ;TODO set cache in network-state
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
                                  (derived/event)))))
