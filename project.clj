(defproject nodp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [aysylu/loom "1.0.0"]
                 [clojurewerkz/money "1.9.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [com.rpl/specter "1.0.0"]
                 [frankiesardo/linked "1.2.9"]
                 [funcool/beicon "2.7.0"]
                 [funcool/cats "2.0.0"]
                 [funcool/cuerdas "2.0.2"]
                 [incanter "1.5.7"]
                 [inflections "0.12.2"]
                 [jarohen/chime "0.2.1"]
                 [org.clojure/core.async "0.3.442"]
                 [org.clojure/math.combinatorics "0.1.3"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/test.check "0.9.0"]
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
  :profiles
  {:dev {:dependencies [[binaryage/devtools "0.9.2"]
                        [com.taoensso/encore "2.90.1"]
                        [figwheel-sidecar "0.5.9"]
                        [org.clojure/tools.namespace "0.2.11"]
                        [spyscope "0.1.6"]]
         :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                        [lein-ancient "0.6.10"]]}})
