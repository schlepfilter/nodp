(ns nodp.hfdp.proxy
  (:require [com.rpl.specter :as s]
            [incanter.distributions :as distributions]
            [nodp.helpers :as helpers]))

(defn- get-ratings-path
  [object]
  [(s/must object) :ratings])

;This definition is harder to read.
;(def get-ratings-path
;  (comp (partial (helpers/flip vector) :ratings) s/must))

(defn- make-set-rating
  [{:keys [object rating]}]
  (partial s/transform*
           (get-ratings-path object)
           (partial (helpers/flip conj) rating)))

(defn- proxy-make-set-rating
  [{:keys [subject object] :as m}]
  (helpers/case-eval subject
                     object identity
                     (-> m
                         (dissoc :subject)
                         make-set-rating)))

(defn- get-rating
  [{:keys [object person]}]
  (->> person
       (s/select-one (get-ratings-path object))
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
