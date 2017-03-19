(ns nodp.rmp.message-filter
  (:require [clojure.string :as str]
            [cats.builtin]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [nodp.helpers :as helpers]
            [nodp.rmp.helpers :as rmp-helpers]))

(defn- make-kind?
  [kind]
  (comp (partial (helpers/flip str/starts-with?) kind)
        :kind))

; This definition is less readable.
;(def make-is-kind?
;  (comp
;    ((m/curry 2 (helpers/flip comp)) :kind)
;    (m/curry 2 (helpers/flip str/starts-with?))))

(defn- make-handle-kind-items
  [kind]
  (fn [items]
    (-> kind
        make-kind?
        (some items)
        (helpers/maybe-if (rmp-helpers/handle-items items)))))

(def handle
  (comp maybe/cat-maybes
        (partial m/<*> (map make-handle-kind-items ["ABC" "XYZ"]))
        vector))

(handle rmp-helpers/a-items rmp-helpers/x-items)