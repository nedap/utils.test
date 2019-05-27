(ns nedap.utils.test.api
  (:require
   [nedap.utils.test.impl :as impl]
   [clojure.walk :as walk])
  #?(:clj (:import
           (clojure.lang IMeta))))

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
                                      (if-let [metadata-map (meta form)]
                                        [(walk/postwalk walker metadata-map)
                                         form]
                                        form)))))))
       (apply =)))
