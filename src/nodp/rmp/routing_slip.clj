(ns nodp.rmp.routing-slip
  (:require [aid.core :as aid]))

(def customer
  {:name    "ABC Inc."
   :tax-id  "123-45-6789"
   :contact {:address-1 "123 Main Street"
             :address-2 "Suite 100"
             :city      "Boulder"
             :state     "CO"
             :zip       "80301"
             :phone     "303-555-1212"}
   :service "99-1203"})

(defn- make-operate
  [verb extractor]
  (comp (partial str "handling register customer to " verb ": ")
        extractor))

(def create-customer
  (make-operate "create a new custoner"
                (partial (aid/flip select-keys) #{:name :tax-id})))

(def keep-contact
  (make-operate "keep contact information" :contact))

(def plan-service
  (make-operate "plan a new customer service" :service))

(def check-credit
  (make-operate "perform credit check" :tax-id))

(def handle-customer
  (juxt create-customer
        keep-contact
        plan-service
        check-credit))

(handle-customer customer)
