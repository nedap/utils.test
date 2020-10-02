(ns nedap.utils.test.matchers
  (:require
   [matcher-combinators.core :as matcher-combinators]
   [nedap.utils.test.impl :as impl]))

(defn matches?
  "returns true if `actual` matches `this`. false otherwise"
  [this actual]
  (matcher-combinators/indicates-match?
   (matcher-combinators/match this actual)))

(defmethod impl/expect-matcher 'match? [_]
  {:pred =
   :pred-sym `matches?
   :assert-expr-sym 'match?})

