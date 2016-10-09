(ns hfdp.facade)

(def get-amp
  (comp (partial assoc {} :turn-on)
        (fn [description] (str description " on"))))

(defn watch-movie
  [movie]
  (:turn-on (get-amp "Top-O-Line Amplifier")))

(watch-movie "Raiders of the Lost Ark")