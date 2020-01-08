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

(def successful-summary {:fail 0, :error 0})

(def unsuccessful-summary {:fail 1, :error 1})

#?(:cljs
   (deftest set-exit-code-for!
     (are [summary existing-code expected] (testing [summary existing-code]
                                             (let [process-object (js-obj "exitCode" existing-code)]
                                               (sut/set-exit-code-for! summary process-object)
                                               (is (= expected
                                                      (-> process-object .-exitCode))))
                                             true)
       successful-summary   nil 0
       successful-summary   -1  -1
       successful-summary   0   0
       successful-summary   1   1
       successful-summary   42  42

       unsuccessful-summary nil 1
       unsuccessful-summary -1  1
       unsuccessful-summary 0   1
       unsuccessful-summary 1   1
       unsuccessful-summary 42  1)))
