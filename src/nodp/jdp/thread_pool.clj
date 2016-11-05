(ns nodp.jdp.thread-pool
  (:require [nodp.helpers :as helpers]
            [cats.core :as m]
            [clojure.string :as str]))

(def make-postfix
  (m/curry 2 (helpers/flip str)))

(defmulti get-short-name identity)

(defmethod get-short-name :potato
  [_]
  "PotatoPeeling")

(defmethod get-short-name :coffee
  [_]
  "CoffeeMaking")

(def get-name
  (comp (make-postfix "Task")
        get-short-name))

(defmulti get-time-per identity)

(defmethod get-time-per :potato
  [_]
  200)

(defmethod get-time-per :coffee
  [_]
  100)

(def get-time
  (helpers/build *
                 (comp get-time-per :kind)
                 :quantity))

(def describe
  (comp (partial str/join " ")
        (juxt (constantly "processing")
              (comp get-name :kind)
              (comp (make-postfix "ms") get-time))))

(def handle
  (juxt (comp (helpers/functionize Thread/sleep) get-time)
        (comp println describe)))

(map handle [{:kind     :potato
              :quantity 3}
             {:kind     :potato
              :quantity 6}
             {:kind     :coffee
              :quantity 2}])
