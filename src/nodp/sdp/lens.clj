(ns nodp.sdp.lens
  (:require [com.rpl.specter :as s]
            [clojure.string :as str]))

(def uk
  {:name "United Kingdom"
   :code "uk"})

(def london
  {:name "London"
   :country uk})

(def buckingham-palace
  {:number 1
   :street "Buckingham Palace Road"
   :city london})

(def castle-builders
  {:name "Castle Builders"
   :address buckingham-palace})

(def switzerland
  {:name "Switzerland"
   :code "CH"})

(def geneva
  {:name "geneva"
   :country switzerland})

(def geneva-address
  {:number 1
   :street "Geneva Lake"
   :city geneva})

(def ivan
  {:name "Ivan"
   :company castle-builders
   :address geneva-address})

(s/transform [:company :address :city :country :code] str/upper-case ivan)

;This is equivalent.
;(update-in ivan [:company :address :city :country :code] str/upper-case)
