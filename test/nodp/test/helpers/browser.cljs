(ns nodp.test.helpers.browser
  (:require [cljs.test :as test]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [nodp.helpers.frp :as frp]
            [nodp.test.helpers :as test-helpers]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  browser
  test-helpers/num-tests
  (test-helpers/restart-for-all [advance* test-helpers/advance]
                                (frp/activate)
                                (advance*)
                                ;TODO test location
                                ;TODO test window
                                true))
