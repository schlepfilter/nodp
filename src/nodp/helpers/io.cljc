(ns nodp.helpers.io
  (:require [cats.context :as ctx]
            [cats.core :as m]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers :include-macros true]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple])
  #?(:cljs (:require-macros [nodp.helpers.io :refer [defcurriedmethod]])))

(defn event
  []
  (->> (nodp.helpers/mempty)
       (ctx/with-context event/context)))

(defmulti call-modifier (comp helpers/get-keyword
                              second
                              vector))

#?(:clj (defmacro defcurriedmethod
          [multifn dispatch-val bindings & body]
          `(helpers/defpfmethod ~multifn ~dispatch-val
                                (helpers/curry ~(count bindings)
                                               (fn ~bindings
                                                 ~@body)))))

(defcurriedmethod call-modifier :event
                  [f e network]
                  (if (event/now? e network)
                    (f (event/get-value e network))))

(def on
  (comp (partial swap! helpers/network-state)
        ((m/curry s/setval*) [:effects s/END])
        vector
        call-modifier))
