(ns example.core
  (:require [advenjure.game :as game]
            [example.room-map :refer [room-map]]
            [example.verbs :refer [verb-map]]))

(enable-console-print!)

(let [game-state (game/make room-map :bedroom)
        finished? #(= (:current-room %) :outside)]
    (game/run game-state finished? "Welcome to the example game! type 'help' if you don't know what to do.\n" verb-map))
