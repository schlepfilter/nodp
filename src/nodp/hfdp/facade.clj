(ns nodp.hfdp.facade
  (:require [clojure.string :as str]
            [clojure.core.match :refer [match]]
            [nodp.helpers :as helpers]))

(def verbs
  {:play     "playing"
   :set-dvd  "setting DVD player to"
   :turn-off "off"
   :turn-on  "on"})

(def env)

(defn- get-action
  [device command]
  (->> (match [command]
              [[verb object]] [(verbs verb) (env object)]
              :else [(verbs command)])
       (concat [(env device)])
       (str/join " ")))

;This definition is less readable.
;(defn- get-action
;  [device command]
;  (str/join " " (concat [(env device)]
;                        (if (keyword? command)
;                          [(command verbs)]
;                          [(-> command
;                               first
;                               verbs)
;                           (-> command
;                               second
;                               env)]))))

(defn- get-device-actions
  [[device & commands]]
  (-> (partial get-action device)
      (map commands)))

(def get-actions
  (partial mapcat get-device-actions))

(defn- get-arguments
  [{:keys [device-commands description]}]
  (->> device-commands
       get-actions
       (cons description)))

(def print-arguments
  (comp helpers/printall
        get-arguments))

(def watch-device-commands
  [[:amp
    :turn-on
    [:set-dvd :dvd]]
   [:dvd
    :turn-on
    [:play :film]]])

(helpers/defcurried make-request
                    [m env]
                    (with-redefs [env env]
                      (print-arguments m)))

(def watch
  (make-request {:description     "Get ready to watch a movie..."
                 :device-commands watch-device-commands}))

(def end-device-commands
  [[:dvd
    :turn-off]])

(def end
  (make-request {:description     "Shutting movie theater down..."
                 :device-commands end-device-commands}))

(let [env {:amp  "Top-O-Line Amplifier"
           :dvd  "Top-O-Line DVD Player"
           :film "Raiders of the Lost Ark"}]
  (watch env)
  (end env))
