(ns nodp.jdp.builder
  (:require [cats.core :as m]
            [cats.monad.maybe :as maybe]
            [cuerdas.core :as cuerdas]
            [aid.core :as help]
            [nodp.helpers :as helpers]))

(defn get-head-or-hair
  [hair-type]
  (case hair-type
    :bald "head"
    "hair"))

(defn- describe-hair-type
  [hair-type-maybe]
  (help/casep hair-type-maybe
              maybe/nothing? "hair"
              (-> hair-type-maybe
                  m/join
                  ((juxt cuerdas/human
                         get-head-or-hair))
                  helpers/space-join)))

(defn- make-get-fragment
  [& fs]
  (comp helpers/space-join
        maybe/cat-maybes
        (apply juxt fs)))

;This definition is harder to read.
;(def make-get-sentence
;  (comp (partial comp
;                 helpers/space-join
;                 maybe/cat-maybes)
;        juxt))

(def describe-hair
  (make-get-fragment (constantly (maybe/just "with"))
                     (comp (partial m/<$> name)
                           help/maybe*
                           :hair-color)
                     (helpers/comp-just describe-hair-type
                                     help/maybe*
                                     :hair-type)))

(defn- make-describe-keyword
  [s]
  (comp (partial str s " ")
        name))

;This definition is harder to read
;(def make-describe-keyword
;  (comp (partial (helpers/flip comp) name)
;        (helpers/curry str 2)))

(def describe-profession
  (make-describe-keyword "This is a"))

(def describe-first-name
  (partial str "named "))

(def describe-weapon
  (make-describe-keyword "and wielding a"))

(def describe-armor
  (make-describe-keyword "wearing"))

(def get-hero
  (make-get-fragment (helpers/comp-just describe-profession
                                     :profession)
                     (helpers/comp-just describe-first-name
                                     :first-name)
                     (helpers/comp-just describe-hair
                                     (partial (help/flip select-keys)
                                              [:hair-type :hair-color]))
                     (helpers/comp-just describe-weapon
                                     :weapon)
                     (comp (partial m/<$> describe-armor)
                           help/maybe*
                           :armor)))

(get-hero {:first-name "Riobard"
           :profession :mage
           :hair-color :black
           :weapon     :daggar})

(get-hero {:first-name "Amberjill"
           :profession :mage
           :hair-color :blond
           :hair-type  :long-curly
           :armor      :chain-mail
           :weapon     :sword})

(get-hero {:first-name "Desmond"
           :profession :thief
           :hair-type  :bald
           :weapon     :bow})
