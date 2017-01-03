(ns nodp.hfdp.command
  (:require [clojure.data :as data]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [clojure.math.combinatorics :as combo]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]
            [nodp.hfdp.helpers :as hfdp-helpers]))

(def slot-n 2)

(def do-path
  [(->> (range slot-n)
        (map s/keypath)
        (apply s/multi-path))
   (s/multi-path :on :off)])

(def control
  (s/setval do-path hfdp-helpers/nop []))

(def environment
  {:actions []
   :undos   []
   :now     {:control control
             :fan     :off}
   :redo    []})

(defn- add-undo
  [state]
  (s/setval [:undos s/END] [state] state))

(defmulti get-action (comp first keys))

(defmethod get-action :light
  [{light :light}]
  (-> (str "Light is dimmed to " light "%")
      maybe/just))

(def location
  "Living Room")

(defn- get-description
  [light]
  (->> light
       name
       (str (case light
              :off ""
              "on "))))

(helpers/defpfmethod get-action :fan
                     (comp maybe/just
                           helpers/space-join
                           (partial conj [location "ceiling fan is"])
                           get-description
                           :fan))

(defmethod get-action :control
  [_]
  (maybe/nothing))

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

(defn- get-actions
  [& commands]
  (-> environment
      ((apply comp (map (partial m/<*> (helpers/curry add-action)) commands)))
      :actions
      maybe/cat-maybes))

(defn- undo
  [state]
  (->> state
       :undos
       last
       (s/setval :actions (:actions state))))

(defn- make-set-button
  [{:keys [slot on off]}]
  (partial s/setval* [:now :control (s/keypath slot)] {:on  on
                                                       :off off}))

(defn make-push-button
  [{:keys [slot on]}]
  (comp (m/join (partial s/select-one*
                         [:now :control (s/must slot) (if on
                                                        :on
                                                        :off)]))
        add-undo))

(def make-make-change
  (comp (helpers/curry s/setval*)
        (partial conj [:now])))

(def make-change-light
  (make-make-change :light))

(def turn-on-light
  (make-change-light 100))

(def turn-off-light
  (make-change-light 0))

(def make-set-fan
  (make-make-change :fan))

(defn- defset-fan
  [fan]
  (eval `(def ~(->> fan
                    name
                    (str "set-fan-")
                    symbol)
           (make-set-fan ~fan))))

(def defsets-fan
  (comp (partial run! defset-fan)
        vector))

(defsets-fan :high :medium :off)

(defn- defpush-button
  [{:keys [slot on] :as m}]
  (eval `(def ~(->> (if on
                      "on"
                      "off")
                    (str "push-button-" slot "-")
                    symbol)
           (make-push-button ~m))))

(def map-key
  (comp (helpers/curry 2 map)
        (helpers/curry 2 array-map)))

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
