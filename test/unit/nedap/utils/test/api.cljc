(ns unit.nedap.utils.test.api
  (:require
   #?(:clj [clojure.test :refer [do-report run-tests deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are run-tests] :refer [use-fixtures do-report]])
   [clojure.string :as string]
   [matcher-combinators.test :refer [match?]]
   [nedap.utils.test.api :as sut])
  #?(:clj (:import (clojure.lang ExceptionInfo))))

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

(deftest meta=
  (testing "Basic equality is analog to that of `clojure.core/=`"
    (are [a b] (testing [:= a b]
                 (= (sut/meta= a b)
                    (= a b)))
      "a"     "a"
      "a"     "b"
      1       1
      1       2
      [1 2 3] [1 2 3]
      [1 2 3] [1 3 2]
      []      []
      []      {}
      nil     1
      nil     nil
      {}      {}
      {1 1}   {1 1}
      {1 1}   {1 2}
      'a      'a
      'a      'b
      :a      :a
      :a      :b))

  (testing "Metadata-wise equality is different to that of `clojure.core/=`"
    (are [a b] (testing [:= a b]
                 (not= (sut/meta= a b)
                       (= a b)))
      []   ^:a []
      [[]] [^:a []]
      'a   (with-meta 'a {:a true})))

  (testing "Metadata-wise equality"
    (are [a b expectation] (testing [:= a b]
                             (= expectation
                                (sut/meta= a b)))

      []           []                true
      []           (with-meta [] {}) true
      []           ^:a []            false
      [[]]         [^:a []]          false
      [1 2 ^:a []] [1 2 ^:a []]      true
      [1 2 ^:a []] [1 2 ^:b []]      false))

  (testing "Medatata metadata (and so on, recursively) also accounts for equality"
    (are [a b expectation] (testing [:= a b]
                             (= expectation
                                (sut/meta= a b)))

      ^{:thing ^:other []} []
      ^{:thing ^:other []} []
      true

      ^{:thing ^:other []} []
      ^{:thing ^:OTHER []} []
      false

      ^{:thing ^{:other ^:final []} []} []
      ^{:thing ^{:other ^:final []} []} []
      true

      ^{:thing ^{:other ^:final []} []} []
      ^{:thing ^{:other ^:FINAL []} []} []
      false

      [^{:thing ^{:other ^:final []} []} []]
      [^{:thing ^{:other ^:final []} []} []]
      true

      [^{:thing ^{:other ^:final []} []} []]
      [^{:thing ^{:other ^:FINAL []} []} []]
      false)))

(deftest macroexpansion=
  (testing "Equality"
    (are [a b] (sut/macroexpansion= a b)
      1                1
      [1 [2]]          [1 [2]]
      (gensym)         (gensym)
      [[1 (gensym) 2]] [[1 (gensym) 2]]))

  (testing "Inequality"
    (are [a b] (not (sut/macroexpansion= a b))
      (gensym)         42
      (gensym)         nil
      [[1 (gensym) 2]] [[1 (gensym) 3]])))

(deftest expect
  (let [a (atom 0)]
    (sut/expect (swap! a inc)
                :to-change @a
                :from 0
                :to 1)

    (sut/expect (swap! a inc)
                (swap! a inc)
                :to-change @a
                :from 1
                :to 3))

  (testing "the macroexpansion evaluates each part exactly once"
    (let [proof (atom [])]
      (sut/expect
       (swap! proof conj :body)
       :to-change (do (swap! proof conj :to-change) true)
       :from      (do (swap! proof conj :from)      true)
       :to        (do (swap! proof conj :to)        true))

      (is (= [:to-change :from :body :to-change :to]
             @proof))))

  (testing "normal exception behaviour"
    (is (thrown-with-msg? #?(:clj ExceptionInfo :cljs js/Error) #"my special failure"
                          (sut/expect (throw (ex-info "my special failure" {})) :to-change 0 :from 0 :to 0))))

  (testing "failures"
    (let [test-result (atom {})
          a (atom 0)]
      (are [form expected] (match? expected
                                   (with-redefs [do-report (partial reset! test-result)]
                                     form
                                     @test-result))
        (sut/expect 0 :to-change 0 :from 0 :to 1)
        '{:type :fail, :expected (clojure.core/= 0 1), :actual (not (clojure.core/= 0 1))}

        (sut/expect (swap! a inc) :to-change @a :from 1 :to 2)
        '{:type :fail, :expected (clojure.core/= (clojure.core/deref a) 2), :actual (not (clojure.core/= 1 2))}

        (sut/expect (swap! a inc) :to-change @a :from 1 :to 3)
        '{:type :fail, :expected (clojure.core/= (clojure.core/deref a) 3), :actual (not (clojure.core/= 2 3))})))

  #?(:clj
     (letfn [(assertion-thrown? [assertion form]
               (try
                 (eval form)
                 false
                 (catch Exception e
                   (or (string/includes? (ex-message (ex-cause e)) assertion)
                       (throw (ex-cause e))))))]

       (testing "asserts correct opons"
         (are [form] (assertion-thrown?
                      "(spec/valid? :nedap.utils.test.impl/expect-options options)"
                      form)
           `(sut/expect 1 :to-tjainge 0 :from 0 :to 1)
           `(sut/expect 1 :to-change 0 :from 0 :to 1 :extra :value)
           `(sut/expect 1 :to-change 0 :from nil :to 1)))

       (testing "asserts at least one body"
         (is (assertion-thrown?
              "(spec/valid? (complement empty?) bodies)"
              `(sut/expect :to-change 0 :from 0 :to 0)))))))
