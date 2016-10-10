(ns hfdp.facade)

(defn turn-on-amp
  [description]
  (println (str description " on")))

(defn watch-movie
  [{:keys [amp]}]
  ((juxt turn-on-amp) amp))

(watch-movie {:amp   "Top-O-Line Amplifier"
              :dvd   "Top-O-Line Amplifier"
              :movie "Raiders of the Lost Ark"})
