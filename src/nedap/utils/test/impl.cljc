(ns nedap.utils.test.impl
  (:require
   #?(:clj [clojure.test] :cljs [cljs.test])
   [clojure.spec.alpha :as spec]
   [clojure.string :as string]
   [clojure.walk :as walk]))

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

(defn expect
  [bodies {:keys [to-change from to]} clj?]
  {:pre [(spec/valid? seq bodies)
         (spec/valid? some? to)
         (spec/valid? some? from)
         (spec/valid? some? to-change)
         (spec/valid? boolean? clj?)]}
  (let [is (if clj? 'clojure.test/is 'cljs.test/is)]
    `(do
       (assert (= ~to-change ~from) "Initial state doesn't match :from")
       ~@bodies
       (~is (= ~to-change ~to)))))
