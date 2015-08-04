(ns rpgpal.core
  (:require [rpgpal.parse :as p]
            [rpgpal.eval :as e]
            [rpgpal.roll :as r]
            [clojure.core.async :refer [chan go-loop <!]]))

(def roll-chan (chan))
(def rand-roller (r/map->RandomRoller {}))
(def chan-roller (r/map->ChanRoller {:roller rand-roller :c roll-chan}))

(go-loop []
  (println (<! roll-chan))
  (recur))

(defn roll [formula]
  (-> formula p/parse (e/eval-tree chan-roller)))