(ns example.verbs
  (:require [advenjure.verbs :refer [make-item-handler make-say-verb]]
            [advenjure.verb-map :refer [default-map add-verb]]))

;; some examples of adding custom verbs to support new commands

;; verbs that just print a message
(def yell (make-say-verb "AAAAHHHH!!!"))

;; verbs that rely on item post/preconditions to do something.
;; the keyword will need to be defined on the item record

(def move (make-item-handler "move" :move))
(def pull (make-item-handler "pull" :pull))
(def push (make-item-handler "push" :push))

;; define the verb map to set the regexes that match to the defined verbs

(def verb-map (-> default-map ;; support all the default verbs
                  (add-verb ["^yell$" "^scream$"] yell) ;; supoprt synonyms
                  (add-verb ["^move (?<item>.*)" "^move$"] move) ;; need the (?<item>.*) bit for autocompletion
                  (add-verb ["^pull (?<item>.*)" "^pull$"] pull)  ;; need the no item version for the "pull what?" response
                  (add-verb ["^push (?<item>.*)" "^push$"] push)))
