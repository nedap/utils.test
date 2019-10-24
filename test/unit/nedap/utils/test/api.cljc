(ns unit.nedap.utils.test.api
  (:require
   #?(:clj [clojure.test :refer [do-report run-tests deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are run-tests] :refer [use-fixtures do-report]])
   [clojure.string :as string]
   [matcher-combinators.test :refer [match?]]
   [nedap.utils.test.api :as sut])
  #?(:clj (:import (clojure.lang ExceptionInfo Compiler$CompilerException))))

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

    (testing "bodies can span more than one expression"
      (sut/expect (swap! a inc)
                  (swap! a inc)
                  :to-change @a
                  :from 1
                  :to 3)))

  (testing "the macroexpansion evaluation"
    (let [proof (atom [])]
      (sut/expect
       (swap! proof conj :body)
       :to-change (do (swap! proof conj :to-change) true)
       :from      (do (swap! proof conj :from)      true)
       :to        (do (swap! proof conj :to)        true))

      (is (= [:to-change :from :body :to-change :to]
             @proof))))

  (testing "works for meta on identity-equal values"
    (let [proof (atom {})]
      (sut/expect (swap! proof with-meta {::test true})
                  :to-change (meta @proof)
                  :from nil
                  :to {::test true})))

  (testing "exception in body"
    (is (thrown-with-msg? #?(:clj ExceptionInfo :cljs js/Error) #"my special failure"
                          (sut/expect (throw (ex-info "my special failure" {})) :to-change 0 :from 0 :to 1))))

  (testing ":to failures"
    (let [test-result (atom {})
          a (atom 0)]
      (are [form expected] (match? expected
                                   (with-redefs [do-report (partial reset! test-result)]
                                     form
                                     @test-result))
        (sut/expect 0 :to-change 0 :from 0 :to 1)
        `{:type :fail, :expected (= 0 1), :actual (~'not (= 0 1))}

        (sut/expect (swap! a inc) :to-change @a :from 0 :to 2)
        `{:type :fail, :expected (= (deref ~'a) 2), :actual (~'not (= 1 2))})))

  #?(:clj
     (testing "macroexpansion-time validation"
       (letfn [(assertion-thrown? [assertion form]
                 (try
                   (eval form)
                   false
                   (catch Compiler$CompilerException e
                     (or (string/includes? (ex-message (ex-cause e)) assertion)
                         (throw (ex-cause e))))))]

         (testing "asserts correct options"
           (are [failure form] (assertion-thrown? failure form)

             "#{:to-change :from :to}"
             `(sut/expect () :to-tjainge 0 :from 0 :to 1)

             "#{:to-change :from :to}"
             `(sut/expect () :to-change 0 :from 0 :to 1 :extra :value)

             "#{:to-change :from :to}"
             `(sut/expect () :missing :keys)

             "#{:to-change :from :to}"
             `(sut/expect () :unexpected () :signature 4 :keys)

             "0 should be different from 0"
             `(sut/expect () :to-change 0 :from 0 :to 0)))

         (testing "asserts at least one body"
           (is (assertion-thrown?
                "(seq bodies)"
                `(sut/expect :to-change 0 :from 0 :to 1))))))))
