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
  :source-paths ["script" "src"]
  :target-path "target/%s"
  :clean-targets ^{:protect false} [:target-path
                                    "resources/public/js"
                                    "resources/public/test/js"]
  :profiles {:dev
             {:plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]
                             [lein-ancient "0.6.10"]
                             [lein-figwheel "0.5.9"]
                             [lein-npm "0.6.2"]]
              :dependencies [[binaryage/devtools "0.9.2"]
                             [figwheel-sidecar "0.5.9"]
                             [org.clojure/tools.namespace "0.2.11"]
                             [spyscope "0.1.6"]]
              :cljsbuild    {:builds
                             [{:id           "app"
                               :source-paths ["src"]
                               :compiler     {:output-to            "resources/public/js/main.js"
                                              :output-dir           "resources/public/js/out"
                                              :main                 nodp.core
                                              :asset-path           "/js/out"
                                              :source-map-timestamp true
                                              :preloads             [devtools.preload]
                                              :external-config      {:devtools/config {:features-to-install :all}}}
                               :figwheel     true}
                              {:id           "test"
                               :source-paths ["src" "test"]
                               :compiler     {:output-to            "resources/public/test/js/main.js"
                                              :output-dir           "resources/public/test/js/out"
                                              :main                 nodp.test.core
                                              :asset-path           "/test/js/out"
                                              :source-map-timestamp true
                                              :preloads             [devtools.preload]
                                              :external-config      {:devtools/config {:features-to-install :all}}}
                               :figwheel true}]}}}
  :npm {:dependencies [[karma "0.13.19"]
                       [karma-cljs-test "0.1.0"]]})
