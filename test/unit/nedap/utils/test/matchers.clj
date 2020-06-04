(ns unit.nedap.utils.test.matchers
  (:require
   [clojure.test :refer [deftest testing are is]]
   [matcher-combinators.standalone :refer [match]]
   [nedap.utils.test.matchers :as sut])
  (:import
   (clojure.lang ExceptionInfo)))

(deftest in-any-order
  (testing "behaves like #'matcher-combinators.matchers/in-any-order"
    (are [actual expected] (= expected (match (sut/in-any-order (range 8)) actual))
      (shuffle (range 8))
      {:match/result :match}

      (range 7)
      {:match/result :mismatch, :mismatch/detail '(6 5 4 3 2 1 0 #matcher_combinators.model.Missing{:expected 7})}))

  (testing "will time out after some time"
    (is (thrown-with-msg? ExceptionInfo #"in-any-order timed out"
          (match (sut/in-any-order (range 8) :timeout 50) (range 7))))))
