(ns example.common
  (:require [advenjure.items :as item]))

(def safe-combination "67288")

(def glass-door (item/make ["glass door" "door"] "needed some cleaning."))

(def wallet (item/make ["wallet"] "It was made of cheap imitation leather."
                       :take true
                       :gender :female
                       :open "I didn't have a dime."
                       :look-in "I didn't have a dime."
                       :dialog ["ME"     "Hi, wallet."
                                "WALLET" "Tsup?"
                                "ME"     "Any cash I can use?"
                                "WALLET" "Sorry."]))
