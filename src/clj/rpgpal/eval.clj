(ns rpgpal.eval
  (:require [rpgpal.roll :as r]))

(declare eval-tree-mm merge-results)

(defn eval-tree [formula-tree roller]
  (let [result (eval-tree-mm {:result 0 :rolls [] :roller roller} formula-tree)]
    (dissoc result :roller)))

(defmulti ^:private eval-tree-mm (fn [_ [type]] type))

(defmethod eval-tree-mm :formula
  [result [_ node]]
  (eval-tree-mm result node))

(defmethod eval-tree-mm :number
  [_ [_ number]]
  {:result (read-string number) :rolls {}})

(defmethod eval-tree-mm :add
  [result [_ & nodes]]
  (let [evaled-nodes (map #(eval-tree-mm result %) nodes)]
    (reduce merge-results result evaled-nodes)))

(defmethod eval-tree-mm :dice
  [result [_ [_ times-value] [_ sides-value]]]
  (let [times (read-string times-value)
        sides (read-string sides-value)
        rolls (r/roll-many (:roller result) times sides)]
    (merge-results result (update-in rolls [:rolls] (fn [rolls] {(keyword sides-value) rolls})))))

(defn- merge-results [r1 r2]
  (-> r1
      (update-in [:result] + (:result r2))
      (update-in [:rolls] #(merge-with concat (:rolls r2) %))))