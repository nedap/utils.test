(ns unit.nedap.utils.test.matchers
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [matcher-combinators.core :as matcher-combinators]
   [matcher-combinators.model :as model]
   [matcher-combinators.result :as result]
   [nedap.utils.test.matchers :as sut]))

(deftest gensym-matcher
  (are [input actual expected] (= expected
                                  (matcher-combinators/-match input actual))
    (sut/gensym)
    'G__250234
    #::result{:type :match
              :weight 0
              :value 'G__250234}

    (sut/gensym)
    "G__250234"
    #::result{:type   :mismatch
              :weight 1
              :value  (model/map->Mismatch {:actual   "G__250234"
                                            :expected "G__"})}

    (sut/gensym)
    1
    #::result{:type   :mismatch
              :weight 1
              :value  (model/map->Mismatch {:actual   1
                                            :expected "G__"})}

    (sut/gensym "abc")
    'abc1234
    #::result{:type :match
              :weight 0
              :value 'abc1234}))
