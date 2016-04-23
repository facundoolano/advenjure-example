(ns example.core
  (:require [advenjure.game :as game]
            [example.rooms :refer [room-map]])
  (:gen-class))

(defn -main
  "Build and run the example game."
  [& args]
  (let [game-state (game/make room-map :bedroom)
        finished? #(= (:current-room %) :outside)]
    (game/run game-state finished? "Welcome to the example game! type 'help' if you don't know what to do.\n")))
