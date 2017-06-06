(ns nodp.hfdp.state
  (:require [aid.core :as aid]
            [com.rpl.specter :as s]
            [nodp.helpers :as helpers]))

(defmacro defmultis
  [[mm-name & mm-names] dispatch-fn]
  (if-not (nil? mm-name)
    `(do (defmulti ~mm-name ~dispatch-fn)
         (defmultis ~mm-names ~dispatch-fn))))

(defmultis [insert turn* dispense refill*] (comp :state
                                                 :machine))
(def constantly-add-action
  (comp helpers/make-add-action constantly))

(def make-set-state
  ((aid/curry s/setval*) [:machine :state]))

(aid/defpfmethod insert :quarterless
                 (comp (constantly-add-action "You inserted a quarter")
                        (make-set-state :has-quarter)))

(aid/defpfmethod insert :sold-out
                 (constantly-add-action "You can't insert a quarter, the machine is sold out"))

(def sold-out?
  (comp (partial > 1)
        :gumball-n
        :machine))

(aid/defpfmethod dispense :sold
                 (comp (partial s/transform*
                                 s/STAY
                                 (fn [environment]
                                   ((aid/casep environment
                                               sold-out? (comp (constantly-add-action "Oops, out of gumballs!")
                                                                (make-set-state :sold-out))
                                               (comp (make-set-state :quarterless)))
                                     environment)))
                        (constantly-add-action "A gumball comes rolling out the slot...")
                        (partial s/transform* [:machine :gumball-n] dec)))

(aid/defpfmethod dispense :sold-out
                 (constantly-add-action "No gumball dispensed"))

(aid/defpfmethod turn* :has-quarter
                 (comp (constantly-add-action "You turned...")
                        (make-set-state :sold)))

(aid/defpfmethod turn* :sold-out
                 (constantly-add-action "You turned, but there are no gumballs"))

(def turn
  (comp dispense
        turn*))

(aid/defpfmethod refill* :sold-out
                 (make-set-state :quarterless))

(defn make-refill
  [gumball-n]
  (comp refill*
        (helpers/make-add-action (comp (partial str "The gumball machine was just refilled; it's new count is: ")
                                    :gumball-n
                                    :machine))
        (partial s/transform* [:machine :gumball-n] (partial + gumball-n))))

(defn- get-environment
  [gumball-n]
  {:machine {:gumball-n gumball-n
             :state     :quarterless}
   :actions []})

(defn- get-actions
  [{:keys [gumball-n commands]}]
  (-> gumball-n
      get-environment
      ((apply comp commands))
      :actions))

(get-actions {:gumball-n 2
              :commands  [turn
                          insert
                          (make-refill 5)
                          turn
                          insert
                          turn
                          insert
                          turn
                          insert]})
