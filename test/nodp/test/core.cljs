(ns ^:figwheel-always nodp.test.core
  (:require [cljs.test :refer-macros [run-all-tests]]
            [nodp.test.helpers.time]
            [nodp.test.helpers.tuple]))

(enable-console-print!)

(run-all-tests)
