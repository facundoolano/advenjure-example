(ns example.bedroom
  (:require [advenjure.items :as item]
            [advenjure.rooms :as room]
            [advenjure.utils :as utils]
            [advenjure.change-rooms :refer [change-rooms]]
            [example.common :refer [safe-combination glass-door wallet]]))

;;; DEFINE ROOM ITEMS AND HOOKS

; use a precondition to decide if drink action can be executed
(defn pre-drink
  [game-state]
  (let [bottle (utils/find-first game-state "bottle")]
    (or (not-empty (:items bottle)) "It was empty")))

; since drink was defined without a generic handler, its side effects can be
; coded as a item-specific post conditions
(defn post-drink
  [old-state new-state]
  (let [bottle (utils/find-first new-state "bottle")]
    (utils/say "Refreshing.")
    (utils/replace-item new-state bottle (assoc bottle :items #{}))))


(def bottle (item/make "bottle" "nothing special about it"
                       :items #{(item/make ["amount of water" "water"] "looks good." :drink {:pre `pre-drink :post `post-drink})}
                       :take true
                       :open true
                       :closed false
                       :close "Didn't want to."
                       :drink {:pre `pre-drink :post `post-drink}))

(def fake-magazine (item/make ["magazine" "naughty magazine"] ; TODO prefer naughty magazine name after reading it
                              "The dust cover read 'Sports Almanac 1950-2000'"
                              :read "Oh là là? Oh là là!? That was no sports magazine..."
                              :take true
                              :gender :female))

; add the magazine item to the room to simulate it being "revealed" behind the bed
; replace the bed item so it can't be moved again
(defn get-moved-bed [bed]
  (let [move-text "I moved it enough already."]
    (merge bed {:move move-text
                :push move-text
                :pull move-text})))

(defn move-bed [old gs]
  (let [old-bed (first (utils/find-item gs "bed"))
        new-bed (get-moved-bed old-bed)
        bedroom (utils/current-room gs)
        new-bedroom (-> bedroom
                        (update-in [:items] item/replace-from old-bed new-bed)
                        (room/add-item fake-magazine "On the floor was a sports magazine."))] ; use a room-specific description of the magazine
    (utils/say "I pushed the bed revealing a magazine.")
    (assoc-in gs [:room-map :bedroom] new-bedroom)))


(def bed (item/make ["bed"] "It was the bed I slept in."
                    :pull "Couldn't move it in that direction."
                    :move "Be more specific."
                    :push {:post `move-bed}))

(def paper (item/make ["paper" "piece of paper"]
                      "it had a number written on it."
                      :take true
                      :read (str "\"" safe-combination "\"")))

(def trash-bin (item/make ["trash bin" "trash" "trash can" "bin"]
                          "Aluminum."
                          :items #{paper}))

(defn go-att [oldgs newgs] (change-rooms newgs :attic))
(defn go-bed [oldgs newgs] (change-rooms newgs :bedroom))

(def ladder-bed (item/make ["ladder" "stepladder" "step ladder"]
                           "It was thin, I think I mentioned it."
                          :use {:post `go-att}
                          :climb-up :attic))

(def ladder-att (item/make ["ladder" "stepladder" "step ladder"]
                           "It was thin, I think I mentioned it."
                           :use {:post `go-bed}
                           :climb-down :bedroom))

;;; DEFINE ROOM AND ADD ALL THE ITEMS
(def bedroom (-> (room/make "Bedroom"
                            "A smelling bedroom. There was an unmade bed near the corner and a door to the north."
                            :initial-description "I woke up in a smelling little bedroom, without windows. By the bed I was laying in was a small table and to the north a glass door.")
                 (room/add-item ladder-bed "A thin ladder leaded up.")
                 (room/add-item bed "") ; empty means skip it while describing, already contained in room description
                 (room/add-item trash-bin "Just by the table, a trash bin.")
                 (room/add-item (item/make "floor" "The floor was scratched near the bed.") "The floor was scratched near the bed.")
                 (room/add-item glass-door "")
                 (room/add-item (item/make ["small table" "table"] "A small bed table."
                                           :items #{wallet bottle (item/make ["reading lamp" "lamp"])}))))

(def attic (-> (room/make "Attic" "A dark attic. There was literally nothing of interest there, as if the room existed merely to prove a point.")
               (room/add-item ladder-att "")))
