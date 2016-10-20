(ns example.rooms
  (:require [clojure.string :as string]
            [advenjure.items :as item]
            [advenjure.rooms :as room]
            [advenjure.utils :as utils]
            [advenjure.ui.output]
            [advenjure.ui.output :refer [print-line]]
            [advenjure.ui.input :refer [prompt-value]]
            #?(:cljs [advenjure.dialogs]
               :clj [advenjure.dialogs :refer [dialog]])
            [example.dialogs :refer [npc-dialog]])
  #?(:cljs (:require-macros [advenjure.dialogs :refer [dialog]])))

;;; DEFINE ROOMS AND ITEMS

; TODO prefer naughty magazine name after reading it
(def fake-magazine (item/make ["magazine" "naughty magazine"]
                              "The dust cover read 'Sports Almanac 1950-2000'"
                              :read "Oh là là? Oh là là!? That was no sports magazine..."
                              :take true
                              :gender :female))

(def wallet-dialog (dialog ("ME" "Hi, wallet.")
                           ("WALLET" "Tsup?")
                           ("ME" "Any cash I can use?")
                           ("WALLET" "Sorry.")))

(def wallet (item/make ["wallet"] "It was made of cheap imitation leather."
                       :take true
                       :gender :female
                       :open "I didn't have a dime."
                       :look-in "I didn't have a dime."
                       :dialog `wallet-dialog))

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

(def glass-door (item/make ["glass door" "door"] "needed some cleaning."))

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

(def safe-combination "67288")

(def paper (item/make ["paper" "piece of paper"]
                      "it had a number written on it."
                      :take true
                      :read (str "\"" safe-combination "\"")))

(def trash-bin (item/make ["trash bin" "trash" "trash can" "bin"]
                          "Aluminum."
                          :items #{paper}))

(def bedroom (-> (room/make "Bedroom"
                            "A smelling bedroom. There was an unmade bed near the corner and a door to the north."
                            :initial-description "I woke up in a smelling little bedroom, without windows. By the bed I was laying in was a small table and to the north a glass door.")
                 (room/add-item bed "") ; empty means skip it while describing, already contained in room description
                 (room/add-item trash-bin "Just by the table, a trash bin.")
                 (room/add-item (item/make "floor" "The floor was scratched near the bed.") "The floor was scratched near the bed.")
                 (room/add-item glass-door "")
                 (room/add-item (item/make ["small table" "table"] "A small bed table."
                                           :items #{wallet bottle (item/make ["reading lamp" "lamp"])}))))


(def door (item/make ["door" "wooden door"] "Looked like oak to me." :locked true))

(def drawer (item/make ["chest drawer" "chest" "drawer"]
                       "It had one drawer."
                       :closed true
                       :items #{(item/make ["bronze key" "key"] "A bronze key." :unlocks door :take true)}))

(def magazine (item/make ["sports magazine" "magazine"]
                         "The cover read 'Sports Almanac 1950-2000'"
                         :read "It told the results of every major sports event till the end of the century."
                         :take true
                         :gender :female))

(defn enter-combination
  [game-state]
  (let [combo (prompt-value "Enter combination: ")
        combo (string/trim combo)
        responses ["No luck." "That wasn't it." "Nope."]]
    (if (= combo safe-combination)
        (do (utils/say "Gotcha!") true)
        (get responses (rand-int (count responses))))))

(defn open-safe
  [old-gs new-gs]
  (let [kb (utils/find-first new-gs "keyboard")
        new-kb (assoc kb :use "The safe was already open.")
        safe (utils/find-first new-gs "safe")
        new-safe (merge safe {:closed false
                              :close "Better left open."})]
    (-> new-gs
     (utils/replace-item kb new-kb)
     (utils/replace-item safe new-safe))))

(def safe-conditions {:pre `enter-combination :post `open-safe})

(def safe (item/make ["safe" "safe box" "strongbox" "strong box"]
                     "Hard to open, all right."
                     :items #{magazine}
                     :closed true
                     :open safe-conditions
                     :unlock "I had to USE the keyboard to open the safe."
                     :locked false
                     :lock "It's already locked."))

(def safe-keyboard (item/make ["keyboard" "safe keyboard" "safe box keyboard"]
                              "A numerical keyboard to unlock the safe box."
                              :use safe-conditions))

(def living (-> (room/make "Living Room"
                           "A living room with a nailed shut window. A wooden door leaded east and a glass door back to the bedroom."
                           :initial-description "The living room was as smelly as the bedroom, and although there was a window, it appeared to be nailed shut. There was a pretty good chance I'd choke to death if I didn't leave the place soon.\nA wooden door leaded east and a glass door back to the bedroom.")
                (room/add-item drawer "There was a chest drawer by the door.")
                (room/add-item door "")
                (room/add-item glass-door "")
                (room/add-item safe "Mounted on one of the walls was a safe box with a numerical keyboard on top of it.")
                (room/add-item safe-keyboard "")
                (room/add-item (item/make ["window"] "It was nailed shut." :closed true :open "It was nailed shut.") "")))

(def npc (item/make ["character" "suspicious looking character" "npc"]
                    "The guy was fat and hairy and was giving me a crooked look." :dialog `npc-dialog))

(def hallway (-> (room/make "Hallway"
                            "A narrow hallway with a door to the west and a big portal to the east."
                            :initial-description "I walked out of the living room and found myself in the west side of a narrow hallway leading to a big portal towards east. I felt closer to the exit.")
                 (room/add-item (item/make "portal") "")
                 (room/add-item (item/make "door") "")
                 (room/add-item npc "A suspicious looking character was guarding the portal.")))

(def outside (room/make "Outside" "I found myself in a beautiful garden and was able to breath again. A new adventure began, an adventure that is out of the scope of this example game."))

(defn can-leave? [gs]
  (let [door (first (utils/find-item gs "wooden door"))]
    (cond (:locked door) "The door was locked."
          (not (contains? (:inventory gs) wallet)) "I couldn't leave without my wallet."
          :else :hallway)))

(defn npc-gone? [gs]
  (if (first (utils/find-item gs "character"))
    "I couldn't, that guy was blocking the portal."
    :outside))

;;; BUILD THE ROOM MAP
(def room-map (-> {:bedroom bedroom
                   :living living
                   :outside outside
                   :hallway hallway}
                  (room/connect :bedroom :north :living)
                  (room/one-way-connect :living :east `can-leave?)
                  (room/one-way-connect :hallway :west :living)
                  (room/one-way-connect :hallway :east `npc-gone?)))
