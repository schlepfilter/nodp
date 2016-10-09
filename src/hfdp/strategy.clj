(ns hfdp.strategy)

(def quack
  (partial println "quack"))

(def mullard-quack
  quack)

(mullard-quack)
