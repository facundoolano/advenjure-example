(ns example.hallway
  (:require [advenjure.utils :as utils]
            [advenjure.items :as item]
            [advenjure.rooms :as room]
            [advenjure.dialogs :as dialog]))

;;; first define all the dialog pieces for the character

;; the bool function will take game-state. If true at execution show the first
;; dialog branch, if false the second (if any)
(def greet-npc (dialog/conditional (dialog/event? :knows-npc)
                                   ["ME" "Hi, NPC."]
                                   ["ME" "Hello!"]))

; randomly select one of the given dialog lines
(def npc-says-hi (dialog/random ["NPC" "Hello."]
                                ["NPC" "Hi."]
                                ["NPC" "Hmmm."]))

;; present the player the given dialog options. The cycle will be repeated while
;; there are available options or until one with the :go-back modifier
;; is executed. Options will be consumed after being executed, unless the
;; :sticky modifier is used.
(def guess-npc
  (dialog/optional
   {:dialog ["ME" "A not politically correct kind of person?"
             "NPC" "No."]}

   {:dialog ["ME" "A non-deterministic polynomial-time complete?"
             "NPC" "No."]}

   {:dialog  ["ME" "A non-player character?"
              "NPC" "That's right."
              (dialog/set-event :knows-npc)]
    :go-back true}

   {:dialog  ["ME" "I give up."]
    :sticky  true
    :go-back true}))

(defn npc-moves
  "Magazine item is removed from inventory, NPC character is removed from room,
  player describes NPC leaving."
  [game-state]
  (let [magazine (utils/find-first game-state "magazine") ; both magazines will work
        npc      (utils/find-first game-state "character")]
    (-> game-state
        (utils/say "And so the NPC took the magazine and left the room.")
        (utils/remove-item magazine)
        (utils/remove-item npc))))

(def npc-dialog-options
  (dialog/optional
   {:dialog  ["ME" "Who are you?"
              "NPC" "I'm an NPC."
              guess-npc]
    :show-if (dialog/not-event? :knows-npc)
    :sticky  true}

   {:dialog ["ME" "Why are you here?"
             "NPC" "The programmer put me here to test dialog trees."]}

   {:dialog ["ME" "Would you move? I'm kind of in a hurry"
             "NPC" "Sorry, I can't let you pass until we exhaust our conversation."
             (dialog/set-event :knows-wont-move)]}

   {:dialog  ["ME" "What do I have to do for you to move?"
              "NPC" "You bring me something interesting to read."
              (dialog/set-event :wants-to-read)]
    :show-if (dialog/event? :knows-wont-move)
    :sticky  true}

   {:dialog  ["ME" "Do you want this magazine?"
              "NPC" "I sure do, sir."
              npc-moves]
    ;; TODO refactor
    :show-if #(and ((dialog/item? "magazine") %) ((dialog/event? :wants-to-read) %))
    ;; TODO nice to have
    ;; :show-if [(item? "magazine") (event? :wants-to-read)]
    :go-back true}

   {:dialog  ["ME" "Bye."
              "NPC" "See you."]
    :sticky  true
    :go-back true}))

;; pack all the greetings and the root optional dialog into one dialog

(def npc-dialog [greet-npc npc-says-hi npc-dialog-options])

;; define the npc character by creating an item and setting its dialog

(def npc (item/make ["character" "suspicious looking character" "npc"]
                    "The guy was fat and hairy and was giving me a crooked look."
                    :dialog `npc-dialog))

;; create the room and put the character in it like a regular item

(def hallway (-> (room/make "Hallway"
                            "A narrow hallway with a door to the west and a big portal to the east."
                            :initial-description "I walked out of the living room and found myself in the west side of a narrow hallway leading to a big portal towards east. I felt closer to the exit."
                            :synonyms ["hall"])
                 (room/add-item (item/make "portal") "")
                 (room/add-item (item/make "door") "")
                 (room/add-item npc "A suspicious looking character was guarding the portal.")))
