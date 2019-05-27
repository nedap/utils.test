(ns nedap.utils.test.api
  (:require
   [clojure.walk :as walk]
   [nedap.utils.speced :as speced]))

(speced/defn ^any? simplify
  "Transforms records into maps and Sequential collections into sets, identity otherwise.

  note: this will remove duplicates"
  [^any? val]
  (walk/postwalk
   (fn [node]
     (cond
       (map? node)        (into {} node)
       (map-entry? node)  node       ;; Keep map entries as is
       (sequential? node) (set node) ;; Make collections into a set to prevent ordering issues
       :else              node))
   val))
