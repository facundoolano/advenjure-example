(ns example.hallway
  (:require [advenjure.utils :as utils]
            [advenjure.items :as item]
            [advenjure.rooms :as room]
            #?(:cljs [advenjure.dialogs :refer [event? not-event? set-event item?]]
               :clj [advenjure.dialogs :refer [event? not-event? set-event item? dialog conditional optional random]]))
  #?(:cljs (:require-macros [advenjure.dialogs :refer [dialog conditional optional random]])))


;;; first define all the dialog pieces for the character

; the bool function will take game-state. If true at execution show the first
; dialog branch, if false the second (if any)
(def greet-npc (conditional (event? :knows-npc)
                            ("ME" "Hi, NPC.")
                            ("ME" "Hello!")))

; randomly select one of the given dialog lines
(def npc-says-hi (random ("NPC" "Hello.")
                         ("NPC" "Hi.")
                         ("NPC" "Hmmm.")))

; present the player the given dialog options. The cycle will be repeated while
; there are available options or until one with the :go-back modifier
; is executed. Options will be consumed after being executed, unless the
; :sticky modifier is used.
(def guess-npc
  (optional
    ("A not politically correct kind of person?"
        (dialog ("ME" "A not politically correct kind of person?")
                ("NPC" "No.")))

    ("A non-deterministic polynomial-time complete?"
         (dialog ("ME" "A non-deterministic polynomial-time complete?")
                 ("NPC" "No.")))

    ("A non-player character?"
         (dialog ("ME" "A non-player character?")
                 ("NPC" "That's right.")
                 (advenjure.dialogs/set-event :knows-npc))
         :go-back)

    ("I give up." (dialog ("ME" "I give up."))
        :sticky :go-back)))

(defn npc-moves
  "Magazine item is removed from inventory, NPC character is removed from room,
  player describes NPC leaving."
  [game-state]
  (let [magazine (first (utils/find-item game-state "magazine")) ; both magazines will work
        npc (first (utils/find-item game-state "character"))]
    (utils/say "And so the NPC took the magazine and left the room.")
    (-> game-state
        (utils/remove-item magazine)
        (utils/remove-item npc))))

(def npc-dialog-options
  (optional
    ("Who are you?"
          (dialog ("ME" "Who are you?")
                  ("NPC" "I'm an NPC.")
                  guess-npc)
          :show-if (advenjure.dialogs/not-event? :knows-npc)
          :sticky)

    ("Why are you here?"
          (dialog ("ME" "Why are you here?")
                  ("NPC" "The programmer put me here to test dialog trees.")))

    ("Would you move? I'm kind of in a hurry"
            (dialog ("ME" "Would you move? I'm kind of in a hurry")
                    ("NPC" "Sorry, I can't let you pass until we exhaust our conversation.")
                    (advenjure.dialogs/set-event :knows-wont-move)))

    ("What do I have to do for you to move?"
           (dialog ("ME" "What do I have to do for you to move?")
                   ("NPC" "You bring me something interesting to read.")
                   (advenjure.dialogs/set-event :wants-to-read))
           :show-if (advenjure.dialogs/event? :knows-wont-move)
           :sticky)

    ("Do you want this magazine?"
         (dialog ("ME" "Do you want this magazine?")
                 ("NPC" "I sure do, sir.")
                 npc-moves)
         :show-if #(and ((advenjure.dialogs/item? "magazine") %) ((advenjure.dialogs/event? :wants-to-read) %))
         :go-back)

    ("Bye." (dialog ("ME" "Bye.")
                    ("NPC" "See you."))
            :sticky
            :go-back)))


;; pack all the greetings and the root optional dialog into one dialog

(def npc-dialog
        (dialog greet-npc npc-says-hi npc-dialog-options))

;; define the npc character by creating an item and setting its dialog

(def npc (item/make ["character" "suspicious looking character" "npc"]
                    "The guy was fat and hairy and was giving me a crooked look." :dialog `npc-dialog))

;; create the room and put the character in it like a regular item

(def hallway (-> (room/make "Hallway"
                            "A narrow hallway with a door to the west and a big portal to the east."
                            :initial-description "I walked out of the living room and found myself in the west side of a narrow hallway leading to a big portal towards east. I felt closer to the exit."
                            :synonyms ["hall"])
                 (room/add-item (item/make "portal") "")
                 (room/add-item (item/make "door") "")
                 (room/add-item npc "A suspicious looking character was guarding the portal.")))
