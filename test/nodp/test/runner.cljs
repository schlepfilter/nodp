(ns nodp.test.runner
  (:require [doo.runner :as runner :include-macros true]
            [nodp.test.helpers.tuple]))

(runner/doo-all-tests)
