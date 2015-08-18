(ns rpgpal.roll
  (:require [clojure.core.async :refer [go >!!]]))

(defprotocol Roller
  (roll [this sides]))

(defrecord RandomRoller []
  Roller
  (roll [_ sides]
    (+ 1 (rand-int sides))))

(defrecord ChanRoller [roller c]
  Roller
  (roll [_ sides]
    (let [result (roll roller sides)]
      (go (>!! c {:result result :sides sides}))
      result)))

(defn roll-many [roller times sides]
  (let [rolls (repeatedly times #(roll roller sides))
        result (reduce + rolls)]
    {:rolls rolls :result result}))