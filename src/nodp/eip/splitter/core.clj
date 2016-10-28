(ns nodp.eip.splitter.core)

(def items
  [{:id    1
    :type  "A"
    :price 23.95}
   {:id 2
    :type "B"
    :price 99.95}
   {:id 3
    :type "C"
    :price 14.95}])

(def handle-item
  (partial str "handling "))

(map handle-item items)
