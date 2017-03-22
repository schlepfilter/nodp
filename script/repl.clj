(ns repl
  (:require [figwheel-sidecar.repl-api :as repl-api]))

(repl-api/start-figwheel!
  {:build-ids ["app" "test"]
   :all-builds
              [{:id           "app"
                :source-paths ["src"]
                :compiler     {:output-to            "resources/public/js/main.js"
                               :output-dir           "resources/public/js/out"
                               :main                 "nodp.core"
                               :asset-path           "/js/out"
                               :source-map-timestamp true
                               :preloads             ['devtools.preload]
                               :external-config      {:devtools/config {:features-to-install :all}}}
                :figwheel     true}
               {:id           "test"
                :source-paths ["src" "test"]
                :compiler     {:output-to            "resources/public/test/js/main.js"
                               :output-dir           "resources/public/test/js/out"
                               :main                 "nodp.test.core"
                               :asset-path           "/test/js/out"
                               :source-map-timestamp true
                               :preloads             ['devtools.preload]
                               :external-config      {:devtools/config {:features-to-install :all}}}
                :figwheel     true}]})

(repl-api/cljs-repl)
