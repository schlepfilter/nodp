(ns repl
  (:require [com.rpl.specter :as s]
            [figwheel-sidecar.repl-api :as repl-api]
            [taoensso.encore :as encore]))

(def app-build
  {:id           "app"
   :source-paths ["src"]
   :compiler     {:output-to            "resources/public/js/main.js"
                  :output-dir           "resources/public/js/out"
                  :main                 "nodp.core"
                  :asset-path           "/js/out"
                  :source-map-timestamp true
                  :preloads             ['devtools.preload]
                  :external-config      {:devtools/config {:features-to-install :all}}}
   :figwheel     true})

(def test-build
  (-> (s/setval [:source-paths s/END] ["test"] app-build)
      (encore/nested-merge {:id       "test"
                            :compiler {:output-to  "resources/public/test/js/main.js"
                                       :output-dir "resources/public/test/js/out"
                                       :main       "nodp.test.core"
                                       :asset-path "/test/js/out"}})))

(repl-api/start-figwheel!
  {:build-ids  ["app" "test"]
   :all-builds [app-build test-build]})

(repl-api/cljs-repl)
