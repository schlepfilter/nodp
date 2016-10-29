(ns nodp.eip.routing-slip
  (:require [nodp.helpers :as helpers]))

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
  [s extractor]
  (comp (partial str "handling register customer to " s)
        extractor))

(def create-customer
  (make-operate "create a new custoner: "
                (partial (helpers/flip select-keys) #{:name :tax-id})))

(def keep-contact
  (make-operate "keep contact information: "
                :contact))

(def check-credit
  (make-operate "perform credit check: "
                :tax-id))

(def handle-customer
  (helpers/build vector create-customer keep-contact check-credit))

(def printall
  (comp helpers/printall handle-customer))

(printall customer)
