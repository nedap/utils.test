(ns unit.nedap.utils.test.impl
  (:require
   #?(:clj [clojure.test :refer [deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are] :refer [use-fixtures]])
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

#?(:clj
   (deftest ref-changed
     (are [form options expected] (= expected
                                     (let [ref (atom {})]
                                       (sut/ref-changed ref form options)))
       (swap! ref assoc :value 1)
       {:to {:value 1}}
       {:type :pass
        :expected {:value 1}
        :actual {:value 1}}

       (swap! ref assoc :value 1)
       {}
       {:type     :pass
        :expected ::sut/changed
        :actual   ::sut/changed}

       (swap! ref assoc :value 2)
       {:to {:value 1}}
       {:type :fail
        :expected {:value 1}
        :actual {:value 2}}

       (constantly true)
       {:to {:value 1} :timeout 1}
       {:type :fail
        :expected {:value 1}
        :actual ::sut/timed-out})))
