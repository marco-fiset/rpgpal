(ns rpgpal.integration-test
  (:require [clojure.test :refer :all]
          [rpgpal.core :refer [roll]]))

(deftest simple-additions
  (testing "Adding integers together"
    (is (= {:result 6 :rolls {}} (roll "2+4")))
    (is (= {:result -2 :rolls {}} (roll "2-4")))
    (is (= {:result 2 :rolls {}} (roll "-2+4")))))

(deftest dice-rolls
  (testing "Simple dice rolls"
    (is (= 3 (-> (roll "3d6") :rolls :6 count)))
    (is (= 4 (-> (roll "4d8") :rolls :8 count)))))

(deftest adding-dice-rolls
  (testing "Adding dice rolls together"
    (is (= 2 (-> (roll "3d6+4d8") :rolls count)))
    (is (= 3 (-> (roll "3d6+4d8") :rolls :6 count)))
    (is (= 4 (-> (roll "3d6+4d8") :rolls :8 count)))))

(deftest adding-rolls-and-numbers
  (testing "Adding rolls and numbers together"
    (is (roll "3d6+4"))))