(ns example.core
  (:require [example.game :refer [run-game]])
  (:gen-class))

(defn -main [& args] (run-game))
