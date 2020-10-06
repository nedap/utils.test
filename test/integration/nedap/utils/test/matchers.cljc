(ns integration.nedap.utils.test.matchers
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [matcher-combinators.standalone :as standalone]
   [matcher-combinators.model :as model]
   [nedap.utils.test.matchers :as sut]))

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
