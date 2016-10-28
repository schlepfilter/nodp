(ns nodp.jdpe.prototype
  (:require [nodp.helpers :as helpers]))

(def saloon1
  {:engine {:turbo false
            :size  1300}
   :color  "UNPAINTED"})

(def saloon2
  saloon1)

(def pickup
  saloon1)

(helpers/printall [saloon1 saloon2 pickup])
