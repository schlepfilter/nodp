(ns nodp.hfdp.state
  (:require [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(defmacro defmultis
  [[mm-name & mm-names] dispatch-fn]
  (if-not (nil? mm-name)
    `(do (defmulti ~mm-name ~dispatch-fn)
         (defmultis ~mm-names ~dispatch-fn))))

(defmultis [insert turn* dispense refill] (comp :state :machine))

(def make-add-action
  (comp ((helpers/curry specter/transform*) :actions)
        (helpers/flip (helpers/curry 2 conj))))

(def make-set-state
  ((helpers/curry specter/setval*) [:machine :state]))

(defmethod insert :quarterless
  [environment]
  (->> environment
       ((make-set-state :has-quarter))
       ((make-add-action "You inserted a quarter"))))

(defmethod insert :sold-out
  [environment]
  (->> environment
       ((make-add-action "You can't insert a quarter, the machine is sold out"))))

(def sold-out?
  (comp (partial > 1) :gumball-n :machine))

(defmethod dispense :sold
  [environment]
  (->> environment
       (specter/transform [:machine :gumball-n] dec)
       ((make-add-action "A gumball comes rolling out the slot..."))
       (specter/transform specter/STAY
                          (fn [environment*]
                            ((if (sold-out? environment*)
                               (comp (make-add-action "Oops, out of gumballs!")
                                     (make-set-state :sold-out))
                               (comp (make-set-state :quarterless)))
                              environment*)))))

(defmethod dispense :sold-out
  [environment]
  (->> environment
       ((make-add-action "No gumball dispensed"))))

(defmethod turn* :has-quarter
  [environment]
  (->> environment
       ((make-set-state :sold))
       ((make-add-action "You turned..."))))

(defmethod turn* :sold-out
  [environment]
  (->> environment
       ((make-add-action "You turned, but there are no gumballs"))))

(def turn
  (comp dispense
        turn*))

;This definition is less readable
;(defmethod insert :quarterless
;  [{actions                :actions
;    {gumball-n :gumball-n} :machine}]
;  {:actions (conj actions "You inserted a quarter")
;   :machine {:gumball-n gumball-n
;             :state     :has-quarter}})

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
              :commands  [turn insert turn insert turn insert]})
