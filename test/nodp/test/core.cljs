(ns ^:figwheel-always nodp.test.core
  (:require [cljs.test :refer-macros [run-all-tests]]
            [nodp.test.helpers.derived.behavior]
            [nodp.test.helpers.io]
            [nodp.test.helpers.location]
            [nodp.test.helpers.primitives.behavior]
            [nodp.test.helpers.primitives.event]
            [nodp.test.helpers.time]
            [nodp.test.helpers.tuple]
            [nodp.test.helpers.window]))

(enable-console-print!)

(run-all-tests)
