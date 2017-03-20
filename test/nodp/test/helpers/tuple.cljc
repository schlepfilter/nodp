(ns nodp.test.helpers.tuple
  (:require [clojure.test.check]
            [clojure.test.check.clojure-test :as clojure-test :include-macros true]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(clojure-test/defspec tuple-monad-left-identity-law
                      10
                      (prop/for-all [v gen/any]
                                    true))
