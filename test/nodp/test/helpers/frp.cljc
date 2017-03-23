(ns nodp.test.helpers.frp
  (:require [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
            [nodp.helpers.frp :as frp]
    #?(:clj
            [clojure.test :as test]
       :cljs [cljs.test :as test :include-macros true])))

(defn fixture
  [f]
  (frp/initialize)
  (f))

(test/use-fixtures :each fixture)
