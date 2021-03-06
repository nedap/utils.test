(ns integration.nedap.utils.test.matchers
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [matcher-combinators.model :as model]
   [matcher-combinators.standalone :as standalone]
   [nedap.utils.test.matchers :as sut])
  #?(:clj (:import (clojure.lang ExceptionInfo))))

(deftest gensym-matcher
  (are [input actual expected] (= expected
                                  (standalone/match input actual))
    {:a (sut/gensym)}
    {:a (gensym)}
    #:match{:result :match}

    {:a (sut/gensym "abc")}
    {:a (gensym "abc")}
    #:match{:result :match}

    {:a (sut/gensym "abcd")}
    {:a 'abc254810}
    {:match/result    :mismatch
     :mismatch/detail {:a (model/map->Mismatch {:actual   'abc254810
                                                :expected "abcd"})}}))

(deftest in-any-order
  (testing "behaves like #'matcher-combinators.matchers/in-any-order"
    (are [actual expected] (= expected (standalone/match (sut/in-any-order (range 8)) actual))
      (reverse (range 8))
      {:match/result :match}

      (range 7)
      {:match/result :mismatch, :mismatch/detail '(6 5 4 3 2 1 0 #matcher_combinators.model.Missing{:expected 7})}))

  #?(:clj
     (testing "will timeout after some time"
       (is (thrown-with-msg? ExceptionInfo #"in-any-order timed out"
                             (standalone/match (sut/in-any-order (range 8) :timeout 50) (range 7)))))))
