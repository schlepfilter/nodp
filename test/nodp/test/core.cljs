(ns ^:figwheel-always nodp.test.core
  (:require [cljs.test :refer-macros [run-all-tests]]
            [nodp.test.helpers.tuple]
            [nodp.test.helpers.primitives.event]
            [nodp.test.helpers.io]))

(enable-console-print!)

(run-all-tests)
