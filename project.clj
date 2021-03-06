(defproject nodp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [aid "0.1.1"]
                 [clojurewerkz/money "1.9.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [com.rpl/specter "1.0.0"]
                 [funcool/beicon "2.7.0"]
                 [funcool/cats "2.0.0"]
                 [funcool/cuerdas "2.0.2"]
                 [frp "0.1.1"]
                 [incanter "1.5.7"]
                 [inflections "0.12.2"]
                 [jarohen/chime "0.2.1"]
                 [org.clojure/math.combinatorics "0.1.3"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.flatland/ordered "1.5.4"]
                 [potemkin "0.4.3"]
                 [prismatic/plumbing "0.5.3"]
                 [riddley "0.1.14"]
                 [thi.ng/geom "0.0.908"]]
  :source-paths ["src"]
  :target-path "target/%s"
  :clean-targets ^{:protect false} [:target-path
                                    "resources/public/js"
                                    "resources/public/test/js"]
  :plugins [[lein-ancient "0.6.10"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [spyscope "0.1.6"]]}})
