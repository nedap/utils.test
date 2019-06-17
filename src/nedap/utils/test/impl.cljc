(ns nedap.utils.test.impl
  (:require
   [clojure.string :as string]
   [clojure.walk :as walk])
  #?(:clj (:import (clojure.lang IReference))))

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

#?(:clj
   (defmacro ref-changed ;;TODO fix name, docs
     [^IReference reference
      body
      {:keys [to timeout]
       :or {to ::changed
            timeout 1000}}]
     `(let [p#     (promise)
            watch# (keyword (gensym))] ;; keyword must be unique per ref!
        (try
          (add-watch ~reference watch#
                     (fn [_# _# old# new#]
                       (when-not (= old# new#)
                         (deliver p# (if (= ::changed ~to)
                                       ::changed
                                       new#)))))

          (let [_# ~body
                value# (deref p# ~timeout ::timed-out)]
            {:type (if (= ~to value#) :pass :fail)
             :expected ~to
             :actual value#})

          (finally
            (remove-watch ~reference watch#))))))
