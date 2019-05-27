(ns unit.nedap.utils.test.api
   #?(:clj  (:require
             [clojure.test :refer [deftest testing are is]]
             [clojure.spec.alpha :as s]
             [nedap.utils.test.api :as sut]
             [nedap.utils.speced :as speced]
             [nedap.utils.spec.api :refer [check!]])
      :cljs (:require
             [cljs.test :refer-macros [deftest testing is are]]
             [nedap.utils.speced :as speced]
             [nedap.utils.spec.api :refer-macros [check!]]
             [nedap.utils.test.api :as sut]
             [cljs.spec.alpha :as s])))

(defrecord Student  [name])
(defrecord School [students])

(deftest simple=
  (testing "Assert behaviour of simple="
    (are [a b] (sut/simple= a b)
      [:a :b :b :c]
      #{:a :b :c}

      {:key [:c :b :a]}
      {:key #{:a :c :b}}

      {:nested {:key [:value :other]}}
      {:nested {:key #{:other :value}}}

      {:students #{{:name "Luna Lovegood"}
                   {:name "Neville Longbottom"}
                   {:name "Harry Potter"}}}
      (->School [(->Student "Luna Lovegood")
                 (->Student "Neville Longbottom")
                 (->Student "Harry Potter")]))))


(s/def ::number number?)

(speced/defn accepts-number [^::number x] x)
(speced/defn ^::number returns-number [x] x)

(deftest spec-violated?
  (are [form spec] (spec-violated? spec form)
    (check! string? 123)    'string?
    (check! ::number "123") ::number
    (accepts-number "1234") ::number
    (returns-number "1234") ::number)

  (testing "No exception swallowing"
    (is (thrown? IllegalArgumentException
                 (throw (IllegalArgumentException. "Wat"))))))
