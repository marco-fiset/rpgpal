(ns rpgpal.parse-test
  (:require [clojure.test :refer :all]
            [rpgpal.parse :as p]))

(defn- formula-is [result formula]
  (is (= result (second (p/parse formula)))))

(deftest root-node-is-formula
  (is (= :formula (first (p/parse "42")))))

(deftest number-nodes
  (formula-is [:number "42"] "42")
  (formula-is [:number "-42"] "-42"))

(deftest dice-nodes
  (formula-is [:dice [:number "3"] [:number "6"]] "3d6"))

(deftest add-nodes
  (formula-is [:add [:number "3"] [:number "6"]] "3+6")
  (formula-is [:add [:number "6"] [:number "-4"]] "6-4")
  (formula-is [:add [:dice [:number "3"] [:number "6"]] [:number "4"]] "3d6+4"))
