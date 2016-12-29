(ns nodp.hfdp.proxy
  (:require [com.rpl.specter :as specter]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers]))

(defn- get-ratings-path
  [object]
  [(specter/must object) :ratings])

;This definition is harder to read.
;(def get-ratings-path
;  (comp (partial (helpers/flip vector) :ratings) specter/must))

(defn- make-set-rating
  [{:keys [object rating]}]
  (partial specter/transform*
           (get-ratings-path object)
           (partial (helpers/flip conj) rating)))

(defn- proxy-make-set-rating
  [{:keys [subject object rating]}]
  (if (= subject object)
    identity
    (make-set-rating {:object object
                      :rating rating})))

(defn- get-rating
  [{:keys [object person]}]
  (->> (specter/select-one (get-ratings-path object) person)
       distributions/mean
       (str "Rating is ")))

(defn- run-commands
  [{:keys [commands person]}]
  ((apply comp commands) person))

(get-rating
  {:object "Joe Javabean"
   :person (run-commands
             {:commands [(proxy-make-set-rating {:object "Joe Javabean"
                                                 :rating 3})
                         (proxy-make-set-rating {:object  "Joe Javabean"
                                                 :subject "Joe Javabean"
                                                 :rating  10})]
              :person   {"Joe Javabean" {:ratings [7]}}})})
