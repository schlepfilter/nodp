(ns nodp.test.helpers.window
  (:require [cljs.test :as test]
            [clojure.test.check]
            [clojure.test.check.clojure-test
             :as clojure-test
             :include-macros true]
            [nodp.helpers.frp :as frp]
            [nodp.helpers.window :as window]
            [nodp.test.helpers :as test-helpers :include-macros true]))

(test/use-fixtures :each test-helpers/fixture)

(clojure-test/defspec
  window
  test-helpers/num-tests
  (test-helpers/restart-for-all [advance test-helpers/advance]
                                (frp/activate)
                                (advance)
                                (= @window/inner-width js/window.innerWidth)))
