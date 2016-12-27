(ns nodp.jdpe.mediator
  (:require [nodp.helpers :as helpers]
            [clojure.string :as str]))

(defn- notify-gearbox-enabled
  []
  "EMS now controlling the gearbox")

(defmacro defcommand
  [f-name f s]
  `(defn- ~f-name
     []
     (flatten [(~f) ~s])))

(defcommand enable-gearbox
            notify-gearbox-enabled "Gearbox enabled")

(defn- notify-accelerator-enabled
  []
  "EMS now controlling the accelerator")

(defcommand enable-accelerator
            notify-accelerator-enabled "Accelerator enabled")

(defn- notify-brake-enabled
  []
  "EMS now controlling the brake")

(defcommand enable-brake
            notify-brake-enabled "Brakes enabled")

(def notify-ignition-turned-on
  (helpers/build concat
                 enable-gearbox
                 enable-accelerator
                 enable-brake))

(defcommand start-ignition
            notify-ignition-turned-on "Ignition turned on")

(start-ignition)

(def accelerate-to-speed
  (partial str "Speed now "))

(accelerate-to-speed 30)

(defn- notify-gear-changed
  []
  "EMS disengaging revs while gear changing")

(defn- set-gear
  [gear]
  (flatten [(notify-gear-changed) (str/join " " ["Now in" gear "gear"])]))

(set-gear "FOURTH")

(defn- notify-accelerator-disabled
  []
  "EMS no longer controlling the accelerator")

(defcommand disable-accelerator
            notify-accelerator-disabled "Accelerator disabled")

(defcommand apply-brake
            disable-accelerator "Now braking")

(apply-brake)
