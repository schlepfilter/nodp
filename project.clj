(defproject nodp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.494"]
                 [clojurewerkz/money "1.9.0"]
                 [com.rpl/specter "1.0.0"]
                 [funcool/beicon "2.7.0"]
                 [funcool/cats "2.0.0"]
                 [funcool/cuerdas "2.0.2"]
                 [incanter "1.5.7"]
                 [inflections "0.12.2"]
                 [org.clojure/math.combinatorics "0.1.3"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/test.check "0.9.0"]
                 [org.flatland/ordered "1.5.4"]
                 [potemkin "0.4.3"]
                 [prismatic/plumbing "0.5.3"]
                 [thi.ng/geom "0.0.908"]]
  :main ^:skip-aot nodp.core
  :target-path "target/%s"
  :profiles {:dev {:plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                                  [lein-ancient "0.6.10"]

                                  [lein-doo "0.1.7"]
                                  [lein-npm "0.6.2"]]
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [spyscope "0.1.6"]]
                   :cljsbuild    {:builds {:test
                                           {:source-paths ["src" "test"]
                                            :compiler     {:output-to  "target/test/main.js"
                                                           :output-dir "target/test/out"
                                                           :main       nodp.test.runner}}}}
                   :doo          {:paths {:karma "node_modules/karma/bin/karma"}}}}
  :npm {:dependencies [[karma "0.13.19"]
                       [karma-cljs-test "0.1.0"]]})
