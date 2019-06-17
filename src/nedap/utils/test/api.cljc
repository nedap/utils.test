(ns nedap.utils.test.api
  (:require
   [clojure.walk :as walk]
   #?(:clj [clojure.test :as test])
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

#?(:clj ;; TODO name
   (defmethod test/assert-expr 'ref-changed? [msg form]
     ;; (is (ref-changed? a expr))
     ;; (is (ref-changed? a expr :to v :timeout 100))
     ;; Asserts that evaluating expr changes reference a to value v.
     ;; both :to and :timeout are optional
     (let [reference     (second form)
           body          (first (nthnext form 2))
           {:as options} (nthnext form 3)]
       `(let [result# (impl/ref-changed ~reference ~body ~options)]
          (test/do-report (assoc result# :message ~msg))))))
