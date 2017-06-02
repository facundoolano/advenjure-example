(ns example.room-map
  (:require [advenjure.rooms :as room]
            [advenjure.utils :as utils]
            [example.common :refer [wallet]]
            [example.bedroom :refer [bedroom attic]]
            [example.living :refer [living]]
            [example.hallway :refer [hallway]]))


;; final room, nothing but a description
(def outside (room/make "Outside" " "))

;; some conditions to leave rooms
(defn can-leave? [gs]
  (let [door (first (utils/find-item gs "wooden door"))]
    (cond (:locked door) "The door was locked."
          (not (contains? (:inventory gs) wallet)) "I couldn't leave without my wallet."
          :else :hallway)))

(defn npc-gone? [gs]
  (if (first (utils/find-item gs "character"))
    "I couldn't, that guy was blocking the portal."
    :outside))

;; define a room map and then set the connections between rooms
(def room-map (-> {:bedroom bedroom
                   :attic attic
                   :living living
                   :outside outside
                   :hallway hallway}
                  (room/connect :bedroom :north :living)
                  (room/connect :bedroom :up :attic)
                  (room/one-way-connect :living :east `can-leave?)
                  (room/one-way-connect :hallway :west :living)
                  (room/one-way-connect :hallway :east `npc-gone?)))
