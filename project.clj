(defproject screen-streamer "0.3.0"
  :description "Screen broadcasting utility"
  :url "https://github.com/erkin/screen-streamer"
  :license {:name "Mozilla Public License 2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async "0.4.490"]
                 [org.clojure/tools.cli "0.4.2"]
                 [seesaw "1.5.0"]]
  :main ^:skip-aot screen-streamer.core
  :pedantic? :warn
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
