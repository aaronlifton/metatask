(defproject metatask "0.0.1-SNAPSHOT"
  :description "Todo list written in clojure"
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {
    :dev {
      :dependencies [
        [midje "1.5.1"]
        [clj-toml "0.3.1"]
        [clojure-lanterna "0.9.4"]
      ]}}
  :main metatask.core)
