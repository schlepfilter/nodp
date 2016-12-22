(ns nodp.hfdp.command
  (:require [clojure.data :as data]
            [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))


(def constantly-nothing
  (constantly (maybe/nothing)))

(def do-path
  [(->> (range 7)
        (map specter/keypath)
        (apply specter/multi-path))
   (specter/multi-path :on :off)])

(def control
  (specter/setval do-path
                  constantly-nothing
                  []))

(def environment
  {:actions []
   :undo    []
   :now     {:control control
             :fan     :off}
   :redo    []})

(def make-change-light
  ((helpers/curry specter/setval*) [:now :light]))

(def turn-on-light
  (make-change-light 100))

(def turn-off-light
  (make-change-light 0))

(defn- add-undo
  [state]
  (specter/transform :undo (partial (helpers/flip conj) state) state))

(defmulti get-action (comp first keys))

(defmethod get-action :light
  [{light :light}]
  (maybe/just (str "Light is dimmed to " light "%")))

;TODO implement get-action with predicate dispatch when core.match supports predicate dispatch
(defmethod get-action :control
  [_]
  (maybe/nothing))

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
       :undo
       last
       (specter/setval :actions (:actions state))))

(defn- make-set-button
  [{:keys [slot on off]}]
  (partial specter/setval* [:now :control (specter/keypath slot)]
           {:on  on
            :off off}))

(defn- make-get-button
  [{:keys [slot on]}]
  (comp (m/join (partial specter/select-one*
                         [:now :control (specter/must slot) (if on
                                                              :on
                                                              :off)]))
        add-undo))

(get-actions
  undo
  (make-get-button {:slot 0
                    :on   true})
  (make-get-button {:slot 0
                    :on   false})
  undo
  (make-get-button {:slot 0
                    :on   false})
  (make-get-button {:slot 0
                    :on   true})
  (make-set-button {:slot 0
                    :on   turn-on-light
                    :off  turn-off-light}))
