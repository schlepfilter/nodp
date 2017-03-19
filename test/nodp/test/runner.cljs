(ns nodp.test.runner
  (:require [doo.runner :as runner :include-macros true]
            [nodp.test.helpers.frp]))

(runner/doo-all-tests)
