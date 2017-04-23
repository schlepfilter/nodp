(ns nodp.helpers.io
  (:require [cats.context :as ctx]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers :include-macros true]
            [nodp.helpers.primitives.event :as event]
            [nodp.helpers.tuple :as tuple])
  #?(:cljs (:require-macros [nodp.helpers.io :refer [defcurriedmethod]])))

(defn event
  []
  (->> (nodp.helpers/mempty)
       (ctx/with-context event/context)))

(defmulti modify-entity! (comp helpers/get-keyword
                               second
                               vector))

#?(:clj (defmacro defcurriedmethod
          [multifn dispatch-val bindings & body]
          `(helpers/defpfmethod ~multifn ~dispatch-val
                                (helpers/curry ~(count bindings)
                                               (fn ~bindings
                                                 ~@body)))))

(defcurriedmethod modify-entity! :event
                  [f e network]
                  (if (event/now? e network)
                    (f (event/get-value e network))))

(def get-latest-maybe
  (comp maybe/just
        helpers/get-latest))

(defmethod modify-entity! :behavior
  [f b]
  (let [past-latest-maybe (atom helpers/nothing)]
    (fn [network]
      (when (not= @past-latest-maybe (get-latest-maybe b network))
        (reset! past-latest-maybe (get-latest-maybe b network))
        (f (helpers/get-latest b network))))))

(def on
  (comp (partial swap! helpers/network-state)
        ((m/curry s/setval*) [:effects s/END])
        vector
        modify-entity!))
