(ns nedap.utils.test.matchers
  "CLJ only while in alpha, eventually CLJS could be supported by using js/Promise"
  (:require
   [nedap.utils.test.impl :refer [when-matcher-combinators]]))

(when-matcher-combinators
 (require 'matcher-combinators.core
          'matcher-combinators.matchers)

 (defrecord InAnyOrder [expected timeout]
   matcher-combinators.core/Matcher
   (match [_this actual]
     (let [result (deref
                   (future (matcher-combinators.core/match (matcher-combinators.matchers/in-any-order expected) actual))
                   timeout
                   ::timeout)]
       (if (#{::timeout} result)
         (throw (ex-info "in-any-order timed out", {:expected expected
                                                    :actual   actual}))
         result))))

 (defn in-any-order [expected & {:keys [timeout]
                                 :or {timeout 5000}}]
   (->InAnyOrder expected timeout)))
