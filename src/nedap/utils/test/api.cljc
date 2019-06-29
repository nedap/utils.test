(ns nedap.utils.test.api
  (:require
   #?(:clj [clojure.test] :cljs [cljs.test])
   [clojure.spec.alpha :as spec]
   [clojure.walk :as walk]
   [nedap.utils.test.impl :as impl])
  #?(:clj (:import (clojure.lang IMeta))))

(defn simple=
  "Check whether all `vals` have similar structure disregarding possible order

  NOTE: this function will disregard duplicates"
  [& vals]
  {:post [(boolean? %)]}
  (apply = (map impl/simplify vals)))

(defn meta=
  "Do all `xs` (and their metadata, and their members' metadata, and also any metametadata) equal?"
  [& xs]
  {:post [(boolean? %)]}
  (->> xs
       (map (fn [x]
              (->> x
                   (walk/postwalk (fn walker [form]
                                    (if-not #?(:clj  (instance? IMeta form)
                                               :cljs (satisfies? IMeta form))
                                      form
                                      (if-let [metadata-map (-> form meta not-empty)]
                                        [(walk/postwalk walker metadata-map)
                                         form]
                                        form)))))))
       (apply =)))

(defn macroexpansion=
  "Do all `xs` equal, when deeming any contained gensyms unconditionally equal?

  Adequate for when the presence of gensyms would hinder a comparison."
  [& xs]
  {:post [(boolean? %)]}
  (letfn [(r [tree]
            (walk/postwalk impl/replace-gensyms tree))]
    (->> xs (map r) (apply =))))

(defmacro run-tests
  "Runs all tests for the given namespaces (defaulting to the current namespace if none given),
  printing the results.

  When invoked under a cljs context, the Unix exit code will be set to 0 or 1, depending on success."
  [& namespaces]
  {:pre [(spec/valid? (spec/coll-of impl/quoted-namespace?)
                      namespaces)]}
  (if-let [clj? (-> &env :ns nil?)]
    `(clojure.test/run-tests ~@namespaces)
    `(cljs.test/run-tests (cljs.test/empty-env ::impl/exit-code-reporter) ~@namespaces)))

(defmacro expect [& forms]
  "Asserts `to-change` yields `from`, and and yields `to` after evaluating all `forms`.

  `(expect (swap! a inc) :to-change @a :from 1 :to 2)`"
  (let [option-length 6
        options (take-last option-length forms)
        bodies  (drop-last option-length forms)
        clj?    (-> &env :ns nil?)]
    (impl/expect bodies options clj?)))
