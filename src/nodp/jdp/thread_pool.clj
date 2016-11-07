(ns nodp.jdp.thread-pool
  (:require [clojure.string :as str]
            [cats.core :as m]
            [nodp.helpers :as helpers]))

(def make-postfix
  (m/curry 2 (helpers/flip str)))

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
  (helpers/build *
                 (comp get-time-per
                       :kind)
                 :quantity))

(defn- instantiate
  [f]
  (fn [& _] (f)))

(def describe
  (comp (partial str/join " ")
        (juxt (instantiate helpers/get-thread-name)
              (comp get-name
                    :kind)
              (comp (make-postfix "ms")
                    get-time))))

(def handle
  (juxt (comp (helpers/functionize Thread/sleep)
              get-time)
        (comp println
              describe)))

(pmap handle [{:kind     :potato
               :quantity 3}
              {:kind     :potato
               :quantity 6}
              {:kind     :coffee
               :quantity 2}])
