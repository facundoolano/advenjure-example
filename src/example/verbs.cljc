(ns example.verbs
  (:require [advenjure.verbs :refer [make-item-verb make-say-verb]]
            [advenjure.verb-map :refer [default-map expand-verb]]))

;; some examples of adding custom verbs to support new commands

;; verbs that just print a message
(def yell (make-say-verb {:commands ["yell" "scream"] :say "AAAAHHHH!!!"}))

;; verbs that rely on item post/preconditions to do something.
;; the keyword will need to be defined on the item record
(def drink (make-item-verb {:commands ["drink"] :kw :drink}))

;; define the verb map to set the regexes that match to the defined verbs

(def verb-map (merge default-map ;; support all the default verbs
                     (expand-verb yell) ;; add yell verb with all of its variations
                     (expand-verb drink))) ;; add drink verb with all of its variations
