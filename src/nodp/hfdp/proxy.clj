(ns nodp.hfdp.proxy
  (:require [aid.core :as aid]
            [com.rpl.specter :as s]
            [incanter.distributions :as distributions]))

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
           (partial (aid/flip conj) rating)))

(defn- proxy-make-set-rating
  [{:keys [subject object] :as m}]
  (aid/case-eval subject
                 object identity
                 (-> m
                         (dissoc :subject)
                         make-set-rating)))

(def get-rating
  (comp (partial str "Rating is ")
        distributions/mean
        (aid/build s/select-one*
                   (comp get-ratings-path
                             :object)
                   :person)))

(defn- run-commands
  [{:keys [commands person]}]
  ((apply comp commands) person))

;This definition is harder to read.
;(def run-commands
;  (m/<*> (comp (partial apply comp) :commands)
;         :person))

(get-rating
  {:object "Joe Javabean"
   :person (run-commands
             {:commands [(proxy-make-set-rating {:object "Joe Javabean"
                                                 :rating 3})
                         (proxy-make-set-rating {:object  "Joe Javabean"
                                                 :subject "Joe Javabean"
                                                 :rating  10})]
              :person   {"Joe Javabean" {:ratings [7]}}})})
