(ns nedap.utils.test.api
  #?(:cljs (:require
            [cljs.core :refer [ExceptionInfo]]
            [cljs.test :refer [do-report]]
            [nedap.utils.test.impl :as impl])
     :clj  (:require
            [clojure.test :refer [do-report]]
            [nedap.utils.test.impl :as impl]))
  #?(:clj  (:import
            (clojure.lang ExceptionInfo))))

(defn simple=
  "Check whether all `vals` have similar structure disregarding possible order

  NOTE: this function will disregard duplicates"
  [& vals]
  (apply = (map impl/simplify vals)))

(defmethod clojure.test/assert-expr 'spec-violated? [msg form]
  ;; (is (spec-violated? s expr))
  ;; Asserts that evaluating expr throws an ExceptionInfo related to spec-symbol s.
  (let [spec-sym (second form)
        body     (nthnext form 2)]
    `(try
       (with-out-str ; silence output
         ~@body)
       (do-report {:type :fail, :message ~msg
                   :expected '~spec-sym, :actual nil})
       (catch ExceptionInfo e#
         (let [spec# (:spec (ex-data e#))]
           (if (= spec# ~spec-sym)
             (do-report {:type :pass, :message ~msg
                         :expected '~spec-sym, :actual nil})
             (do-report {:type :fail, :message ~msg
                         :expected '~spec-sym, :actual spec#})))))))
