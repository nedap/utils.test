(ns nedap.utils.test.matchers
  (:refer-clojure :exclude [gensym])
  (:require
   [clojure.string :as string]
   [matcher-combinators.core :as matcher-combinators]
   [matcher-combinators.matchers :as matchers]
   [matcher-combinators.model :as model]
   [matcher-combinators.result :as result]
   [nedap.utils.test.impl :as impl]))

(defn matches?
  "returns true if `actual` matches `this`. false otherwise"
  [this actual]
  (matcher-combinators/indicates-match?
   (matcher-combinators/match this actual)))

(defmethod impl/expect-matcher 'match? [_]
  {:pred-sym-failure "`to-change` does not match? `from`: (not (match? %s %s))"
   :pred-failure "`from` is not allowed to equal `to`: %s"
   :assert-expr-sym 'match?
   :pred-sym `matches?
   :pred =})

(defrecord Gensym [expected]
  matcher-combinators/Matcher
  (-matcher-for [this] this)
  (-matcher-for [this _] this)
  (-match [_this actual]
    (if (and (symbol? actual)
             (= (str expected) (string/replace (str actual) #"(.?)(\d)+" "$1")))
      {::result/type   :match
       ::result/value  actual
       ::result/weight 0}
      {::result/type   :mismatch
       ::result/value  (model/->Mismatch expected actual)
       ::result/weight 1})))

(defn gensym
  "Matcher that will match when given symbol matches the `expected` after the tailing numbers are stripped.

  If prefix is not supplied, the prefix is 'G__'"
  ([] (gensym "G__"))
  ([prefix-string]
   (map->Gensym {:expected prefix-string})))

#?(:clj
   (defrecord InAnyOrder [expected timeout]
     matcher-combinators/Matcher
     (-matcher-for [this] this)
     (-matcher-for [this _] this)
     (-match [_this actual]
       (let [result (deref
                     (future (matcher-combinators/match (matchers/in-any-order expected) actual))
                     timeout
                     ::timeout)]
         (if (#{::timeout} result)
           (throw (ex-info "in-any-order timed out", {:expected expected
                                                      :actual   actual}))
           result)))))

#?(:clj
   (defn in-any-order
     "Matcher that will match when the given a list that is the same as the
     `expected` list but with elements in a different order.

     cancels evaluation when `:timeout` is reached (default 5000ms).

     drop-in replacement for #'matcher-combinators.matchers/in-any-order"
     [expected & {:keys [timeout]
                  :or {timeout 5000}}]
     #?(:clj (map->InAnyOrder {:expected expected
                               :timeout  timeout})
        :cljs (matchers/in-any-order expected)))

   :cljs
   (defn in-any-order
     "Matcher that will match when the given a list that is the same as the
     `expected` list but with elements in a different order.

     Similar to Midje's `(just expected :in-any-order)`.

     equal to #'matcher-combinators.matchers/in-any-order"
     [expected & {}]
     (matchers/in-any-order expected)))
