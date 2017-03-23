(ns nodp.test.helpers.frp
  (:require [nodp.helpers :as helpers]
            [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop :include-macros true]
    #?(:clj
            [clojure.test :as test]
       :cljs [cljs.test :as test :include-macros true])))

(defn fixture
  [f]
  (helpers/initialize)
  (f))

(test/use-fixtures :each fixture)
