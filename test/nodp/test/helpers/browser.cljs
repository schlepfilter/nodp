(ns nodp.test.helpers.browser
  (:require [cljs.test :as test]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.location :as location]
            [nodp.helpers.window :as window]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  browser
  test-helpers/num-tests
  (test-helpers/restart-for-all [advance test-helpers/advance]
                                (frp/activate)
                                (advance)
                                (= @location/pathname js/location.pathname)
                                (= @window/inner-height js/innerHeight)
                                (= @window/inner-width js/innerWidth)))
