(ns unit.nedap.utils.test.api
  (:require
   #?(:clj  [clojure.test :refer [deftest testing are is use-fixtures]]
      :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.test.api :as sut]))

(deftest simplify
  (are [input expected] (= expected
                           (sut/simplify input))
    nil
    nil

    [:a :b :c]
    #{:c :b :a}

    {:key [:a :b :c]}
    {:key #{:a :c :b}}

    {:key :value}
    {:key :value}

    {:nested {:key [:value]}}
    {:nested {:key #{:value}}}

    {:nested {:key :value}}
    {:nested {:key :value}}))

(defrecord Student  [name])
(defrecord School [students])

(deftest record-comparing
  (let [data     {:students #{{:name "Luna Lovegood"}
                              {:name "Neville Longbottom"}
                              {:name "Harry Potter"}}}
        hogwarts (->School [(->Student "Luna Lovegood")
                            (->Student "Neville Longbottom")
                            (->Student "Harry Potter")])
        simplified (sut/simplify hogwarts)]

    (testing "record comparison"
      (is (not= data hogwarts))
      (is (= data simplified)))

    (testing "partial matches"
      (is (:students simplified)
          {:name "Harry Potter"}))))
