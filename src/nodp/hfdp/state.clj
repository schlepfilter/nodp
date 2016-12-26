(ns nodp.hfdp.state
  (:require [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(defmacro defmultis
  [[mm-name & mm-names] dispatch-fn]
  (if-not (nil? mm-name)
    `(do (defmulti ~mm-name ~dispatch-fn)
         (defmultis ~mm-names ~dispatch-fn))))

(defmultis [insert turn* dispense refill*] (comp :state
                                                 :machine))

(def make-add-action
  (comp ((helpers/curry specter/transform*) :actions)
        (helpers/flip (helpers/curry 2 conj))))

(def make-set-state
  ((helpers/curry specter/setval*) [:machine :state]))

(helpers/defpfmethod insert :quarterless
                     (comp (make-add-action "You inserted a quarter")
                           (make-set-state :has-quarter)))

(helpers/defpfmethod insert :sold-out
                     (make-add-action "You can't insert a quarter, the machine is sold out"))

(def sold-out?
  (comp (partial > 1)
        :gumball-n
        :machine))

(helpers/defpfmethod dispense :sold
                     (comp (partial specter/transform*
                                    specter/STAY
                                    (fn [environment]
                                      ((if (sold-out? environment)
                                         (comp (make-add-action "Oops, out of gumballs!")
                                               (make-set-state :sold-out))
                                         (comp (make-set-state :quarterless)))
                                        environment)))
                           (make-add-action "A gumball comes rolling out the slot...")
                           (partial specter/transform* [:machine :gumball-n] dec)))

(helpers/defpfmethod dispense :sold-out
                     (make-add-action "No gumball dispensed"))

(helpers/defpfmethod turn* :has-quarter
                     (comp (make-add-action "You turned...")
                           (make-set-state :sold)))

(helpers/defpfmethod turn* :sold-out
                     (make-add-action "You turned, but there are no gumballs"))

(def turn
  (comp dispense
        turn*))

(helpers/defpfmethod refill* :sold-out
                     (make-set-state :quarterless))

(defn make-refill
  [gumball-n]
  (comp refill*
        (partial specter/transform*
                 specter/STAY
                 (fn [environment]
                   (specter/transform
                     :actions
                     (partial (helpers/flip conj)
                              (str "The gumball machine was just refilled; it's new count is: "
                                   (-> environment
                                       :machine
                                       :gumball-n)))
                     environment)))
        (partial specter/transform* [:machine :gumball-n] (partial + gumball-n))))

(defn- get-environment
  [gumball-n]
  {:machine {:gumball-n gumball-n
             :state     :quarterless}
   :actions []})

(defn- get-actions
  [{:keys [gumball-n commands]}]
  (-> (get-environment gumball-n)
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
