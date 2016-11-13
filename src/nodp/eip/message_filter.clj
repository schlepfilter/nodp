(ns nodp.eip.message-filter
  (:require [clojure.string :as str]
            [cats.builtin]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [nodp.helpers :as helpers]
            [nodp.eip.helpers :as eip-helpers]))

(defn- make-is-kind?
  [kind]
  (comp (partial (helpers/flip str/starts-with?) kind)
        :kind))

; This definition is less readable.
;(def make-is-kind?
;  (comp
;    ((m/curry 2 (helpers/flip comp)) :kind)
;    (m/curry 2 (helpers/flip str/starts-with?))))

(defn- wrap-maybe
  [expr]
  (if (nil? expr)
    (maybe/nothing)
    (maybe/just expr)))

(defmacro mif
  [& more]
  `(wrap-maybe (if ~@more)))

(defn- make-handle-kind-items
  [kind]
  (fn [items]
    (mif (some (make-is-kind? kind) items)
         (eip-helpers/handle-items items))))

(def handle-items-collection
  (comp (partial map m/join)
        (partial remove maybe/nothing?)
        (partial m/<*> (map make-handle-kind-items ["ABC" "XYZ"]))))

(defn- printall
  [& more]
  (-> more
      handle-items-collection
      helpers/printall))

(printall eip-helpers/a-items eip-helpers/x-items)
