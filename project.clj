(defproject gof "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [funcool/cats "2.0.0"]
                 [riddley "0.1.12"]
                 [org.flatland/ordered "1.5.4"]]
  :main ^:skip-aot gof.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
