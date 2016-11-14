(defproject nodp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojurewerkz/money "1.9.0"]
                 [com.rpl/specter "0.13.0"]
                 [funcool/cats "2.0.0"]
                 [inflections "0.12.2"]
                 [org.clojure/math.combinatorics "0.1.3"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.flatland/ordered "1.5.4"]
                 [prismatic/plumbing "0.5.3"]
                 [riddley "0.1.12"]
                 [thi.ng/geom "0.0.908"]]
  :main ^:skip-aot nodp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
