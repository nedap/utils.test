(ns unit.nedap.utils.test.api
  (:require
   #?(:clj [clojure.test :refer [do-report run-tests deftest testing are is use-fixtures]] :cljs [cljs.test :refer-macros [deftest testing is are run-tests] :refer [use-fixtures do-report]])
   [clojure.string :as string]
   [matcher-combinators.matchers :as matchers]
   [matcher-combinators.test :refer [match?]]
   [nedap.utils.test.api :as sut]
   [nedap.utils.test.impl :as impl]
   [nedap.utils.test.matchers])
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

;; invalid expect-matchers for testing
(defmethod impl/expect-matcher 'wrong-pred-sym [_]
  {:pred =
   :pred-sym "not-a-symbol",
   :pred-sym-failure "fail"
   :pred-failure "fail"
   :assert-expr-sym '=,})
(defmethod impl/expect-matcher 'missing-pred [_]
  {:pred-sym `=,
   :pred-sym-failure "fail"
   :pred-failure "fail"
   :assert-expr-sym '=})
(defmethod impl/expect-matcher 'custom-failure [_]
  {:pred =
   :pred-sym `=,
   :pred-sym-failure "My custom failure!"
   :pred-failure "My other custom failure!"
   :assert-expr-sym '=})

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

  (testing ":with `clojure.test/=`"
    (let [a (atom 0)]
      (sut/expect (swap! a inc)
                  :to-change @a
                  :from 0
                  :to 1
                  :with =)))

  (testing ":with match?"
    (let [a (atom {::key ::value
                   ::other-key ::other-value})]
      (sut/expect (swap! a assoc ::key ::new-value)
                  :to-change @a
                  :from {::key ::value}
                  :to {::key ::new-value}
                  :with match?))
    (let [a (atom [3 2 1])]
      (sut/expect (reset! a [4 3 2 1])
                  :to-change @a
                  :from (matchers/in-any-order [1 2 3])
                  :to (matchers/in-any-order [1 2 3 4])
                  :with match?)))

  (testing "the macroexpansion evaluation"
    (let [proof (atom [])]
      (sut/expect
       (swap! proof conj :body)
       :to-change (do (swap! proof conj :to-change) true)
       :from      (do (swap! proof conj :from)      true)
       :to        (do (swap! proof conj :to)        true))

      (is (= [:to-change :from :body :to :to-change]
             @proof))))

  (testing "possible to test metadata"
    (let [proof (atom {})]
      (sut/expect (swap! proof with-meta {::test true})
                  :to-change @proof
                  :from {}
                  :to ^::test {})

      (let [proof (atom ^::test {})]
        (sut/expect (swap! proof with-meta {})
                    :to-change @proof
                    :from ^::test {}
                    :to {}))))

  (testing "exception in body"
    (is (thrown-with-msg? #?(:clj ExceptionInfo :cljs js/Error) #"my special failure"
                          (sut/expect (throw (ex-info "my special failure" {})) :to-change 0 :from 0 :to 1))))

  ;; this test relies on #'with-redefs to capture the test-report
  (when #?(:clj (not (System/getProperty "clojure.compiler.direct-linking"))
           :cljs true)
    (testing ":to failures"
      (let [test-result (atom {})
            a (atom 0)
            c (atom 0)]
        (are [form expected] (match? expected
                                     (with-redefs [do-report (partial reset! test-result)]
                                       form
                                       @test-result))
          (sut/expect 0 :to-change 0 :from 0 :to 1)
          `{:type :fail, :expected (sut/meta= 1 0), :actual (~'not (sut/meta= 1 0))}

          (sut/expect 0 :to-change {} :from {} :to ^::test {})
          `{:type :fail, :expected (sut/meta= {} {}), :actual (~'not (sut/meta= {} {}))}

          (sut/expect (swap! a inc) :to-change @a :from 0 :to 2)
          `{:type :fail, :expected (sut/meta= 2 (~'clojure.core/deref ~'a)), :actual (~'not (sut/meta= 2 1))}

           ;; change matcher to `=`
          (sut/expect 0 :to-change 0 :from 0 :to 1 :with =)
          `{:type :fail, :expected (~'= 1 0), :actual ~'(not (= 1 0))}

          (sut/expect (swap! a inc) :to-change @a :from 1 :to 3 :with =)
          `{:type :fail, :expected (~'= 3 (~'clojure.core/deref ~'a)), :actual ~'(not (= 3 2))}

          ;; change matcher to `match?`
          (sut/expect 0 :to-change 0 :from 0 :to 1 :with match?)
          #?(:clj
             `{:type :fail,
               :expected (~'match? 1 0),
               :actual {:summary (~'not (~'matcher-combinators.clj-test/match? 1 0))
                        :match-result {:matcher-combinators.result/type :mismatch
                                       :matcher-combinators.result/value {:expected 1 :actual 0}
                                       :matcher-combinators.result/weight 1}}}
             :cljs
             `{:type :matcher-combinators/mismatch
               :expected (~'match? 1 0)
               :actual (~'not (~'matcher-combinators.cljs-test/match? 1 0))})

          (sut/expect (swap! c inc) :to-change @c :from 0 :to 2 :with match?)
          #?(:clj
             `{:type :fail,
               :expected (~'match? 2 (clojure.core/deref ~'c)),
               :actual {:summary (~'not (~'matcher-combinators.clj-test/match? 2 1)),
                        :match-result {:matcher-combinators.result/type :mismatch,
                                       :matcher-combinators.result/value {:expected 2, :actual 1},
                                       :matcher-combinators.result/weight 1}}}
             :cljs
             `{:type :matcher-combinators/mismatch
               :expected (~'match? 2 (~'clojure.core/deref ~'c))
               :actual (~'not (~'matcher-combinators.cljs-test/match? 2 1))})))))

  #?(:clj
     (when *assert*
       (testing "macroexpansion-time validation"
         (letfn [(assertion-thrown? [assertion form]
                   (try
                     (eval form)
                     false
                     (catch Throwable e
                       (or (string/includes? (ex-message (clojure.stacktrace/root-cause e)) assertion)
                           (throw (clojure.stacktrace/root-cause e))))))]

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

               "`from` is not allowed to equal `to`: 0"
               `(sut/expect () :to-change 0 :from 0 :to 0)

               "`from` is not allowed to equal `to`: 0"
               `(sut/expect () :with ~'= :to-change 0 :from 0 :to 0)

               "`from` is not allowed to equal `to`: ^#:unit.nedap.utils.test.api{:wat true} {}"
               `(sut/expect () :to-change 0 :from ^::wat {} :to ^::wat {})

               "My other custom failure!"
               `(sut/expect () :to-change 0 :from 0 :to 0 :with ~'custom-failure)

               "No method in multimethod 'expect-matcher' for dispatch value: unknown"
               `(sut/expect () :to-change 0 :from 0 :to 1 :with ~'unknown)

               "invalid :pred-sym registered for: wrong-pred-sym, got: \"not-a-symbol\""
               `(sut/expect () :to-change 0 :from 0 :to 1 :with ~'wrong-pred-sym)

               "invalid :pred registered for: missing-pred, got: nil"
               `(sut/expect () :to-change 0 :from 0 :to 1 :with ~'missing-pred)

               "`to-change` does not equal `from`: (not (meta= 1 0))"
               `(sut/expect () :to-change 0 :from 1 :to 2)

               "`to-change` does not equal `from`: (not (= 1 0))"
               `(sut/expect () :to-change 0 :from 1 :to 2 :with ~'=)

               "`to-change` does not match? `to`: (not (match? #matcher_combinators.core.InAnyOrder{:expected [1 2]} [2]))"
               `(sut/expect () :to-change [2] :from (matchers/in-any-order [1 2]) :to [2] :with ~'match?)

               "`from` is not allowed to equal `to`: (matcher-combinators.matchers/in-any-order [1 2])"
               `(sut/expect () :to-change [1] :from (matchers/in-any-order [1 2]) :to (matchers/in-any-order [1 2]) :with ~'match?)))

           (testing "asserts at least one body"
             (is (assertion-thrown?
                  "(seq bodies)"
                  `(sut/expect :to-change 0 :from 0 :to 1)))))))))
