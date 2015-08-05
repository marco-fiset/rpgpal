(ns rpgpal.core
  (:require [rpgpal.parse :as p]
            [rpgpal.eval :as e]
            [rpgpal.roll :as r]
            [clojure.core.async :refer [chan go-loop <!]]))

(def roll-chan (chan))
(def rand-roller (r/map->RandomRoller {}))
(def chan-roller (r/map->ChanRoller {:roller rand-roller :c roll-chan}))

(defn roll [formula]
  (-> formula p/parse (e/eval-tree rand-roller)))

(defn roll-until [formula result]
  (loop [r (roll formula) i 1]
    (if (= result (:result r))
      i
      (recur (roll formula) (inc i)))))

(defn rolls-until [formula result]
  (cons (roll-until formula result) (lazy-seq (rolls-until formula result))))