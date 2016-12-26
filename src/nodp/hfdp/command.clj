(ns nodp.hfdp.command
  (:require [clojure.data :as data]
            [clojure.string :as str]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [clojure.math.combinatorics :as combo]
            [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(def constantly-nothing
  (-> (maybe/nothing)
      constantly))

(def slot-n 2)

(def do-path
  [(->> (range slot-n)
        (map specter/keypath)
        (apply specter/multi-path))
   (specter/multi-path :on :off)])

(def control
  (specter/setval do-path constantly-nothing []))

(def environment
  {:actions []
   :undos   []
   :now     {:control control
             :fan     :off}
   :redo    []})

(defn- add-undo
  [state]
  (specter/transform :undos (partial (helpers/flip conj) state) state))

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
       (str (if (= light :off)
              ""
              "on "))))

(helpers/defpfmethod get-action :fan
                     (comp maybe/just
                           (partial str/join " ")
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
  (specter/transform :actions
                     (partial (helpers/flip conj)
                              (-> (data/diff after before)
                                  first
                                  :now
                                  get-action))
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
       (specter/setval :actions (:actions state))))

(defn- make-set-button
  [{:keys [slot on off]}]
  (partial specter/setval* [:now :control (specter/keypath slot)] {:on  on
                                                                   :off off}))

(defn- make-push-button
  [{:keys [slot on]}]
  (comp (m/join (partial specter/select-one*
                         [:now :control (specter/must slot) (if on
                                                              :on
                                                              :off)]))
        add-undo))

(def make-make-change
  (comp (helpers/curry specter/setval*)
        (partial conj [:now])))

(def make-change-light
  (make-make-change :light))

(def turn-on-light
  (make-change-light 100))

(def turn-off-light
  (make-change-light 0))

(def make-set-fan
  (make-make-change :fan))

(defmacro defset-fan
  [fan]
  `(def ~(->> fan
              name
              (str "set-fan-")
              symbol)
     (make-set-fan ~fan)))

(helpers/defdefs defsets-fan
                 defset-fan)

(defsets-fan :high :medium :off)

(defmacro defpush-button
  [{:keys [slot on] :as m}]
  `(def ~(->> (if on
                "on"
                "off")
              (str "push-button-" slot "-")
              (symbol))
     (make-push-button ~m)))

(def map-key
  (comp (helpers/curry 2 map)
        (helpers/curry 2 array-map)))

(def get-buttons
  (comp (partial map (partial apply merge))
        (partial combo/cartesian-product
                 ((map-key :on) [true false]))
        (map-key :slot)
        range))

(helpers/defdefs defpush-buttons*
                 defpush-button)

(def defpush-buttons
  (comp (partial apply (helpers/functionize defpush-buttons*))
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
