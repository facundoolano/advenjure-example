(ns example.game
  (:require
   [advenjure.game :as game]
   [example.verbs :refer [verb-map]]
   [example.room-map :refer [room-map]]
   [advenjure.plugins.map :refer [map-on-every-room]]
   [advenjure.plugins.points :refer [points]]))

(def game-state (-> (game/make room-map :bedroom)
                    (game/use-plugin map-on-every-room)
                    (game/use-plugin points)))

(defn finished? [gs] (= (:current-room gs) :outside))

(defn run-game []
  (game/run game-state
    finished?
    :start-message "Welcome to the example game! type 'help' if you don't know what to do.\n \n"
    :end-message "I found myself in a beautiful garden and was able to breath again. A new adventure began, an adventure that is out of the scope of this example game.\n  \n  \nThe End."
    :verb-map verb-map))
