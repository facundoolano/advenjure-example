(ns example.living
  (:require [clojure.string :as string]
            [advenjure.items :as item]
            [advenjure.rooms :as room]
            [advenjure.utils :as utils]
            [advenjure.ui.input :refer [prompt-value]]
            [example.common :refer [safe-combination glass-door wallet]]
            #?(:clj [advenjure.async :refer [alet]]))
  #?(:cljs (:require-macros [advenjure.async :refer [alet]])))


;;; DEFINE ROOM ITEMS AND HOOKS

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
  ;; using the alet macro to hide the async nature of user input in clojurescript
  (alet [combo (prompt-value "Enter combination: ")
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

(defn move-picture
  [old gs]
  (let [portrait (utils/find-first gs "portrait")
        new-protrait (merge portrait {:move "Enough moving." :pull "Enough moving."})
        new-living (-> (utils/current-room gs)
                    (room/add-item safe "Mounted on one of the walls was a safe box with a numerical keyboard on top of it; below the portrait I took down.")
                    (room/add-item portrait "")
                    (room/add-item safe-keyboard ""))]
    (utils/say "I took down the painting, revealing a safe box mounted on the wall.")
    (assoc-in gs [:room-map :living] new-living)))


(def portrait (item/make ["portrait" "painting" "picture"]
                "It was painting of a middle-aged man, rather ugly if you asked me."
                :take "Too big to fit in my pocket."
                :move {:post `move-picture}
                :pull {:post `move-picture}))


;;; DEFINE ROOM AND ADD ALL THE ITEMS

(def living (-> (room/make "Living Room"
                           "A living room with a nailed shut window. A wooden door leaded east and a glass door back to the bedroom."
                           :initial-description "The living room was as smelly as the bedroom, and although there was a window, it appeared to be nailed shut. There was a pretty good chance I'd choke to death if I didn't leave the place soon.\nA wooden door leaded east and a glass door back to the bedroom.")
                (room/add-item drawer "There was a chest drawer by the door.")
                (room/add-item door "")
                (room/add-item glass-door "")
                (room/add-item portrait "A portrait ocuppied a very prominent place on one of the walls.")
                (room/add-item (item/make ["window"] "It was nailed shut." :closed true :open "It was nailed shut.") "")))
