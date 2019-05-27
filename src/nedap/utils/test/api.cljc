(ns nedap.utils.test.api
  (:require
   [nedap.utils.test.impl :as impl]))

(defn simple=
  "Check whether all `vals` have similar structure disregarding possible order

  NOTE: this function will disregard duplicates"
  [& vals]
  (apply = (map impl/simplify vals)))
