(ns rpgpal.core
  (:require [rpgpal.parse :as p]
            [rpgpal.eval :as e]
            [rpgpal.roll :as r]
            [clojure.core.async :refer [chan go-loop <!]]))

(def roll-chan (chan))
(def rand-roller (r/map->RandomRoller {}))
(def chan-roller (r/map->ChanRoller {:roller rand-roller :c roll-chan}))

;(go-loop []
;  (println (<! roll-chan))
;  (recur))

(defn roll [formula]
  (-> formula p/parse (e/eval-tree chan-roller)))

(defn roll-until [formula result]
  (loop [r (roll formula), i 1, rolls []]
    (if (= result (:result r))
      {:count i :rolls (conj rolls (:result r))}
      (recur (roll formula) (inc i) (conj rolls (:result r))))))

(defn rolls-until [formula result]
  (repeatedly #(roll-until formula result)))

(defn- avg [coll]
  (if (empty? coll)
    0
    (/ (reduce + coll) (count coll))))

(defn avg-rolls-until [times formula result]
  (->> (rolls-until formula result)
       (take times)
       (map :count)
       avg))

(defn formula-result-frequencies [formula sample-size]
  (let [rolls (take sample-size (repeatedly #(roll formula)))
        results (sort (map :result rolls))]
    (into (sorted-map) (frequencies results))))