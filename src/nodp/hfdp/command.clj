(ns nodp.hfdp.command
  (:require [clojure.data :as data]
            [clojure.math.combinatorics :as combo]
            [cats.builtin]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as s]
            [aid.core :as help]
            [nodp.helpers :as helpers]))

(def slot-n 2)

(def do-path
  [(->> (range slot-n)
        (map s/keypath)
        (apply s/multi-path))
   (s/multi-path :on :off)])

(def control
  (s/setval do-path help/nop []))

(def environment
  {:actions []
   :undos   []
   :now     {:control control
             :fan     :off}})

(def add-undo
  (partial helpers/transfer* [:undos s/END] vector))

(defmulti get-action (comp first
                           keys))

(def get-percent
  (partial (help/flip str) "%"))

(help/defpfmethod get-action :light
                  (comp maybe/just
                        (partial str "Light is dimmed to ")
                        get-percent
                        :light))

(def location
  "Living Room")

(defn- get-preposition
  [light]
  (case light
    :off help/nothing
    (maybe/just "on")))

(def get-description
  (comp helpers/space-join
        maybe/cat-maybes
        (juxt get-preposition
              (helpers/comp-just name))))

;This definition can be decomposed.
;(defn- get-description
;  [light]
;  (->> light
;       name
;       (str (case light
;              :off ""
;              "on "))))

(help/defpfmethod get-action :fan
                  (helpers/comp-just helpers/space-join
                                     (partial conj [location "ceiling fan is"])
                                     get-description
                                     :fan))

(defmethod get-action :control
  [_]
  help/nothing)

;This definition is harder to read.
;(helpers/defpfmethod get-action :control
;                     (constantly (maybe/nothing)))

(defn- add-action
  [before after]
  (s/setval [:actions s/END]
            (-> (data/diff after before)
                first
                :now
                get-action
                vector)
            after))

;This definition is harder to read.
;(def add-action
;  (helpers/build (partial s/setval* [:actions s/END])
;                 (comp vector
;                       get-action
;                       :now
;                       first
;                       (partial apply data/diff)
;                       reverse
;                       vector)
;                 (comp second
;                       vector)))

(defn- get-actions
  [& commands]
  (-> environment
      ((apply comp (map (partial m/<*> (help/curry add-action)) commands)))
      :actions
      maybe/cat-maybes))

(def undo
  (help/build (partial s/setval* :actions)
              :actions
              (comp last
                    :undos)))

(def make-set-button
  (help/build (help/curry s/setval*)
              (comp (partial conj [:now :control]) s/keypath :slot)
              ((help/flip select-keys) [:on :off])))

(defn make-push-button
  [{:keys [slot on]}]
  (comp (m/join (partial s/select-one*
                         [:now :control (s/must slot) (if on
                                                        :on
                                                        :off)]))
        add-undo))

(def make-make-change
  (comp (help/curry s/setval*)
        (partial conj [:now])))

(def make-change-light
  (make-make-change :light))

(def turn-on-light
  (make-change-light 100))

(def turn-off-light
  (make-change-light 0))

(def make-set-fan
  (make-make-change :fan))

(def defset-fan
  (help/build (partial intern *ns*)
              (comp symbol
                    (partial str "set-fan-")
                    name)
              make-set-fan))

(def defsets-fan
  (comp (partial run! defset-fan)
        vector))

(defsets-fan :high :medium :off)

(defn- defpush-button
  [{:keys [slot on] :as m}]
  (intern *ns* (->> (if on
                      "on"
                      "off")
                    (str "push-button-" slot "-")
                    symbol)
          (make-push-button m)))

(def map-key
  (comp (help/curry map)
        (help/curry array-map)))

(def get-buttons
  (comp (partial map (partial apply merge))
        (partial combo/cartesian-product
                 ((map-key :on) [true false]))
        (map-key :slot)
        range))

(def defpush-buttons
  (comp (partial run! defpush-button)
        get-buttons))

(defpush-buttons slot-n)

(get-actions
  undo
  push-button-1-on
  undo
  push-button-0-off
  push-button-0-on
  (make-set-button {:slot 1
                    :on   set-fan-high
                    :off  set-fan-off})
  (make-set-button {:slot 0
                    :on   set-fan-medium
                    :off  set-fan-off})
  undo
  push-button-0-on
  push-button-0-off
  undo
  push-button-0-off
  push-button-0-on
  (make-set-button {:slot 0
                    :on   turn-on-light
                    :off  turn-off-light}))
