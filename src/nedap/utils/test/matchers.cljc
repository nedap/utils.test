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
  {:pred-sym-failure "`to-change` does not match? `to`: (not (match? %s %s))"
   :pred-failure "`from` is not allowed to equal `to`: %s"
   :assert-expr-sym 'match?
   :pred-sym `matches?
   :pred =})
