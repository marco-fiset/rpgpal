(ns rpgpal.eval-test
  (:require [clojure.test :refer :all]
            [rpgpal.eval :as e]
            [rpgpal.roll :as r]))

(defrecord MaxRoller []
  r/Roller
  (roll [_ sides] sides))

(def roller (MaxRoller.))

(deftest eval-numbers
  (testing "Numbers evaluate to themselves"
    (is (= 42 (:result (e/eval-tree [:number "42"] roller))))
    (is (= -42 (:result (e/eval-tree [:number "-42"] roller))))))

(deftest eval-add
  (testing "Adding numbers together"
    (is (= 42 (:result (e/eval-tree [:add [:number "24"] [:number "18"]] roller))))
    (is (= -42 (:result (e/eval-tree [:add [:number "98"] [:number "1"] [:number "-141"]] roller))))))

(deftest eval-dice-rolls
  (testing "Dice rolls should attach rolls to the result"
    (let [result (e/eval-tree [:dice [:number "3"] [:number "6"]] roller)]
      (is (= 3 (->> result :rolls (mapcat second) count)))))
  (testing "Dice rolls should be groupped by sides"
    (let [result (e/eval-tree [:add [:dice [:number "3"] [:number "6"]] [:dice [:number "4"] [:number "8"]]] roller)]
      (is (= 3 (-> result :rolls :d6 count)))
      (is (= 4 (-> result :rolls :d8 count))))))

(deftest eval-add-dice-rolls
  (testing "Dice rolls should be addded together"
    (is (= 26 (:result (e/eval-tree [:add [:dice [:number "3"] [:number "6"]] [:dice [:number "1"] [:number "8"]]] roller))))))