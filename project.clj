(defproject nodp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojurewerkz/money "1.9.0"]
                 [com.rpl/specter "0.13.1"]
                 [funcool/beicon "2.7.0"]
                 [funcool/cats "2.0.0"]
                 [funcool/cuerdas "2.0.2"]
                 [incanter "1.5.7"]
                 [inflections "0.12.2"]
                 [org.clojure/math.combinatorics "0.1.3"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.flatland/ordered "1.5.4"]
                 [potemkin "0.4.3"]
                 [prismatic/plumbing "0.5.3"]
                 [thi.ng/geom "0.0.908"]]
  :plugins [[lein-ancient "0.6.10"]]
  :main ^:skip-aot nodp.core
  :target-path "target/%s"
  :profiles {:dev     {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                      [spyscope "0.1.6"]]}
             :uberjar {:aot :all}})
