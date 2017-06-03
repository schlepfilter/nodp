(ns nodp.jdp.thread-pool
  (:require [help.core :as help]
            [nodp.helpers :as helpers]))

(def make-postfix
  (help/flip (help/curry str)))

(helpers/defmultis-identity get-short-name get-time-per)

(helpers/defmethods :potato
                    {get-short-name "PotatoPeeling"
                     get-time-per   200})

(helpers/defmethods :coffee
                    {get-short-name "CoffeeMaking"
                     get-time-per   100})

(def get-name
  (comp (make-postfix "Task")
        get-short-name))

(def get-time
  (help/build *
              (comp get-time-per :kind)
              :quantity))

(defn- instantiate
  [f]
  (fn [& _] (f)))

(def describe
  (comp helpers/space-join
        (juxt (instantiate helpers/get-thread-name)
              (comp get-name
                    :kind)
              (comp (make-postfix "ms")
                    get-time))))

(def handle
  (juxt (comp (help/functionize Thread/sleep)
              get-time)
        (comp println
              describe)))

(pmap handle [{:kind     :potato
               :quantity 3}
              {:kind     :potato
               :quantity 6}
              {:kind     :coffee
               :quantity 2}])
