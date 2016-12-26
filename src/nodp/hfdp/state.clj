(ns nodp.hfdp.state
  (:require [com.rpl.specter :as specter]
            [nodp.helpers :as helpers]))

(defmulti insert (comp :state :machine))

(defmethod insert :quarterless
  [environment]
  (->> environment
       (specter/setval [:machine :state] :has-quarter)
       (specter/transform :actions
                          (partial (helpers/flip conj)
                                   "You inserted a quarter"))))

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
              :commands  [insert]})
