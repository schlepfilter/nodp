(ns hfdp.facade
  (:require [clojure.string :as str]
            [hfdp.helpers :as helpers]))

(def verbs
  {:play     "playing"
   :set-dvd  "setting DVD player to"
   :turn-off "off"
   :turn-on  "on"})

(def env)

(defn- get-sentence
  [verb & more]
  (->> (conj (rest more) (verb verbs) (first more))
       (str/join " ")))

(defn- get-action
  [device command]
  (let [device-name (device env)]
    (if (keyword? command)
      (get-sentence command device-name)
      (get-sentence (first command)
                    device-name
                    ((second command) env)))))

(defn- get-device-actions
  [[device & commands]]
  (-> (partial get-action device)
      (map commands)))

(def get-actions
  (partial mapcat get-device-actions))

(defn- get-arguments
  [{:keys [device-commands description]}]
  (->> (get-actions device-commands)
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

(defn watch
  [env]
  (with-redefs [env env]
    (-> {:description     "Get ready to watch a movie..."
         :device-commands watch-device-commands}
        print-arguments)))

(def end-device-commands
  [[:dvd
    :turn-off]])

(defn end
  [env]
  (with-redefs [env env]
    (-> {:description     "Shutting movie theater down..."
         :device-commands end-device-commands}
        print-arguments)))

(let [env {:amp  "Top-O-Line Amplifier"
           :dvd  "Top-O-Line DVD Player"
           :film "Raiders of the Lost Ark"}]
  (watch env)
  (end env))
