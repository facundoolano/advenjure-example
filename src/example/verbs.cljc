(ns example.verbs
  (:require [advenjure.verbs :refer [make-item-handler make-say-verb]]
            [advenjure.verb-map :refer [default-map add-verb]]))

;; some examples of adding custom verbs to support new commands

;; verbs that just print a message
(def yell (make-say-verb "AAAAHHHH!!!"))

;; verbs that rely on item post/preconditions to do something.
;; the keyword will need to be defined on the item record
(def drink (make-item-handler "drink" :drink))

;; define the verb map to set the regexes that match to the defined verbs

(def verb-map (-> default-map ;; support all the default verbs
                  (add-verb ["^yell$" "^scream$"] yell) ;; supoprt synonyms
                  (add-verb ["^drink (?<item>.*)" "^drink$"] drink))) ;; need the (?<item>.*) bit for autocompletion
                                                                     ;; need the no item version for the "pull what?" response
