(ns nodp.hfdp.state
  (:require [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(defmacro defmultis
  [[mm-name & mm-names] dispatch-fn]
  (if-not (nil? mm-name)
    `(do (defmulti ~mm-name ~dispatch-fn)
         (defmultis ~mm-names ~dispatch-fn))))

(defmultis [insert turn dispense refill] (comp :state :machine))

(def make-add-action
  (comp ((helpers/curry specter/transform*) :actions)
        (helpers/curry 2 (helpers/flip conj))))

(defmethod insert :quarterless
  [environment]
  (->> environment
       (specter/setval [:machine :state] :has-quarter)
       ((make-add-action "You inserted a quarter"))))

(defmethod dispense :sold
  [environment]
  (->> environment
       (specter/transform [:machine :gumball-n] dec)
       ((make-add-action "A gumball comes rolling out the slot..."))
       (specter/transform :machine
                          (fn [{gumball-n :gumball-n :as machine}]
                            (specter/setval :state
                                            (if (< 0 gumball-n)
                                              :quarterless
                                              :sold-out)
                                            machine)))))

(defmethod turn :has-quarter
  [environment]
  (->> environment
       (specter/setval [:machine :state] :sold)
       ((make-add-action "You turned..."))
       dispense))

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
              :commands  [turn insert]})
