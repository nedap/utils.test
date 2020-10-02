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

     (defn set-exit-code-for! [summary process-object]
       (assert (map? summary))
       (if-not (cljs.test/successful? summary)
         (set! (.-exitCode process-object) 1)
         (when-not (.-exitCode process-object)
           (set! (.-exitCode process-object) 0))))

     (defmethod cljs.test/report [::exit-code-reporter :end-run-tests] [summary]
       (set-exit-code-for! summary js/process))))

(defmulti expect-matcher
          "Given a symbol returns :assert-expr-sym, :pred-sym and :pred.

           - `:assert-expr-sym` is used to construct the `is` form to preserve clojure.tes/assert-expr behaviour.
           - `:pred` is used to assert validity on compile time.
           - `:pred-sym` is used to assert validity on runtime.

           These nuances exist for interoperability between clj and cljs runtime."
          identity)

(defmethod expect-matcher '= [_]
  {:assert-expr-sym '=
   :pred-sym `=
   :pred =})

(defn expect
  [bodies {:keys [to-change from to with] :as opts} clj?]
  {:pre [(spec/valid? boolean? clj?)]}
  (assert (seq bodies) "bodies can't be empty")
  (assert (= #{:to-change :from :to} (set (keys (dissoc opts :with)))) (pr-str opts))
  (let [{:keys [pred-sym pred assert-expr-sym]} (expect-matcher with)
        is (if clj? 'clojure.test/is 'cljs.test/is)]
    (assert (ifn? pred)
            (str "invalid :pred registered for: " with ", got: " (pr-str pred)))
    (assert (qualified-symbol? pred-sym)
            (str "invalid :pred-sym registered for: " with ", got: " (pr-str pred-sym)))
    (assert (symbol? assert-expr-sym)
            (str "invalid :assert-expr-sym registered for: " with ", got: " (pr-str assert-expr-sym)))
    (assert (not (pred from to))
            (binding [*print-meta* true]
              (str (pr-str from) " should not match " (pr-str to))))
    (assert (some? to-change) (pr-str to-change))

    `(do
       (let [to-change# ~to-change
             from# ~from]
         (assert (~pred-sym to-change# from#)
                 (binding [*print-meta* true]
                   (str (pr-str to-change#) " does not match expected from: " (pr-str from#)))))
       ~@bodies
       (~is (~assert-expr-sym ~to-change ~to)))))
