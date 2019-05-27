(ns unit.nedap.utils.test.impl
  (:require
   #?(:clj  [clojure.test :refer [deftest testing are is use-fixtures]]
      :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
   [nedap.utils.test.impl :as sut]))

(deftest simplify
  (testing "assert expected transformations by simplify"
    (are [input expected] (= expected
                             (sut/simplify input))
      nil
      nil

      [:a :b :c]
      #{:c :b :a}

      [:a :b :b :c]
      #{:a :b :c}

      {:key [:a :b :c]}
      {:key #{:a :c :b}}

      {:key :value}
      {:key :value}

      {:nested {:key [:value]}}
      {:nested {:key #{:value}}}

      {:nested {:key :value}}
      {:nested {:key :value}})))
