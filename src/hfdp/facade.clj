(ns hfdp.facade)

(defn turn-on-amp
  [description]
  (println (str description " on")))

(defn watch-movie
  [movie]
  ((juxt turn-on-amp) "Top-O-Line Amplifier"))

(watch-movie "Raiders of the Lost Ark")
