(ns nedap.utils.test.impl
  (:require
   #?(:clj [clojure.test] :cljs [cljs.test])
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [clojure.walk :as walk])
  #?(:clj (:import (clojure.lang IMeta))))

(defn simplify
  "Transforms records into maps and Sequential collections into sets, identity otherwise.

  NOTE: this will remove duplicates"
  [val]
  (walk/postwalk
   (fn [node]
     (cond
       (map? node)        (into {} node)
       (map-entry? node)  node       ;; Keep map entries as is
       (sequential? node) (set node) ;; Make collections into a set to prevent ordering issues
       :else              node))
   val))

(defn meta=
  [xs]
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

(defn replace-gensyms [form]
  (if (and (symbol? form)
           ;; some known gensym prefixes that clojure.core uses:
           (or (-> form str (string/starts-with? "G__"))
               (-> form str (string/starts-with? "p__"))
               (-> form str (string/includes? "__auto__"))))
    ::a-gensym
    form))

(defn quoted-namespace? [x]
  (or (symbol? x)
      (and (sequential? x)
           (= 'quote (first x))
           (symbol? (second x)))))

#?(:cljs
   (do
     (derive ::exit-code-reporter :cljs.test/default)
     (defmethod cljs.test/report [::exit-code-reporter :end-run-tests] [summary]
       (if (cljs.test/successful? summary)
         (set! (.-exitCode js/process) 0)
         (set! (.-exitCode js/process) 1)))))

(defn different?
  "true if xs differ in identity or metadata"
  [& xs]
  (or (apply not= xs)
      (not (meta= xs))))

(defn expect
  [bodies {:keys [to-change from to] :as opts} clj?]
  {:pre [(spec/valid? boolean? clj?)]}
  (assert (seq bodies) "bodies can't be empty")
  (assert (= #{:to-change :from :to} (set (keys opts))) (pr-str opts))
  (assert (different? from to) (str (pr-str from) " should be different from " (pr-str to)))
  (assert (some? to-change) (pr-str to-change))

  (let [is (if clj? 'clojure.test/is 'cljs.test/is)]
    `(do
       (let [to-change# ~to-change
             from# ~from]
         (assert (= to-change# from#) (str (pr-str to-change#) " does not match expected :from (" from# ")")))
       ~@bodies
       (~is (= ~to-change ~to)))))
