(ns nedap.utils.test.api
  (:require
   #?(:clj [clojure.test] :cljs [cljs.test])
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

#?(:cljs
   (do
    (derive ::exit-code-reporter :cljs.test/default)
    (defmethod cljs.test/report [::exit-code-reporter :end-run-tests] [summary]
      (if (cljs.test/successful? summary)
        (set! (.-exitCode js/process) 0)
        (set! (.-exitCode js/process) 1)))))

(defmacro run-tests
  "Runs all tests in the given namespaces; prints results.
  Defaults to current namespace if none given.

  when cljs exit-code is set to non-zero when tests fail"
  [& namespaces]
  (let [clj? (-> &env :ns nil?)]
    (if clj?
      `(clojure.test/run-tests ~@namespaces)
      `(cljs.test/run-tests (cljs.test/empty-env ::exit-code-reporter) ~@namespaces))))
