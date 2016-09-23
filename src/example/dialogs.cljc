(ns example.dialogs
  (:require #?(:cljs [advenjure.dialogs :refer [event? not-event? set-event item?]]
               :clj [advenjure.dialogs :refer [event? not-event? set-event item? dialog conditional optional random]])
            [advenjure.utils :as utils])
  #?(:cljs (:require-macros [advenjure.dialogs :refer [dialog conditional optional random]])))

(def PLAYER "ME")
(def NPC "NPC")

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
  (let [magazine (first (utils/find-item game-state "magazine"))
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

(def npc-dialog
        (dialog greet-npc npc-says-hi npc-dialog-options))

