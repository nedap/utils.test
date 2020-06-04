(ns nedap.utils.test.api
  (:require
   #?(:clj [clojure.test] :cljs [cljs.test])
   [clojure.spec.alpha :as spec]
   [clojure.walk :as walk]
   [nedap.utils.test.impl :as impl]))

(defn ^:deprecated simple=
  "Check whether all `vals` have similar structure disregarding possible order

  NOTE: this function will disregard duplicates"
  [& vals]
  {:post [(boolean? %)]}
  (apply = (map impl/simplify vals)))

(defn meta=
  "Do all `xs` (and their metadata, and their members' metadata, and also any metametadata) equal?"
  [& xs]
  {:post [(boolean? %)]}
  (impl/meta= xs))

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

(defmacro expect
  "Asserts (via `#'clojure.test/is`) that the expression denoted by `to-change` changes from `from`, to `to`.

  `(expect (swap! a inc) :to-change @a :from 1 :to 2)`"
  [& forms]
  (let [options (->> (reverse forms)
                     (partition 2)
                     (take-while (fn [[_val key]]
                                   (keyword? key)))
                     (reduce (fn [memo [val key]]
                               (assoc memo key val)) {}))
        bodies  (drop-last (* 2 (count options)) forms)
        clj?    (-> &env :ns nil?)]
    (impl/expect bodies options clj?)))
