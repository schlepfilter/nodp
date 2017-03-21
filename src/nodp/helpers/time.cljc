(ns nodp.helpers.time
  (:refer-clojure :exclude [time])
  (:require [cats.protocols :as p]
            [cats.util :as util]
    #?@(:clj  [
            [clj-time.coerce :as c]
            [clj-time.core :as t]]
        :cljs [[cljs-time.coerce :as c]
               [cljs-time.core :as t]]))
  #?(:clj
     (:import (clojure.lang IDeref))))

(defrecord Time
  [x]
  p/Contextual
  (-get-context [_]
    (reify
      p/Semigroup
      (-mappend [_ x* y*]
        (Time. (max @x* @y*)))
      p/Monoid
      (-mempty [_]
        (Time. 0))
      p/Context))
  #?@(:clj  [Comparable
             (compareTo [x* y*]
               (compare @x* @y*))
             IDeref
             (deref [_] x)]
      :cljs [IComparable
             (-compare [x* y*]
                       (compare @x* @y*))
             IDeref
             (-deref [_] x)])
  p/Printable
  (-repr [_]
    (str "#[time " (pr-str x) "]")))

(util/make-printable Time)

(defn time
  [x]
  (Time. x))

(defn now
  []
  (-> (t/now)
      c/to-long
      time))
