(ns example.common
  (:require [advenjure.items :as item]
            #?(:clj [clojure.core.async :refer [go go-loop <! >! chan take!]]
               :cljs [cljs.core.async :refer [<! >! chan take!]])
            #?(:clj [advenjure.dialogs :refer [dialog]]
               :cljs [advenjure.dialogs]))
  #?(:cljs (:require-macros [advenjure.dialogs :refer [dialog]])))

(def safe-combination "67288")

(def glass-door (item/make ["glass door" "door"] "needed some cleaning."))

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
