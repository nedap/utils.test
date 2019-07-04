(ns dev
  (:require
   [clj-java-decompiler.core :refer [decompile]]
   [clojure.java.javadoc :refer [javadoc]]
   [clojure.pprint :refer [pprint]]
   [clojure.repl :refer [apropos dir doc find-doc pst source]]
   [clojure.test :refer [run-all-tests run-tests]]
   [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
   [criterium.core :refer [quick-bench]]
   [formatting-stack.branch-formatter :refer [format-and-lint-branch!]]
   [formatting-stack.project-formatter :refer [format-and-lint-project!]]
   [lambdaisland.deep-diff]
   [nedap.utils.test.api :refer :all]))

(set-refresh-dirs "src" "test" "dev")

(defn suite []
  (refresh)
  (run-all-tests #".*\.nedap\.utils\.test\..*"))

(defn unit []
  (refresh)
  (run-all-tests #"unit\.nedap\.utils\.test\..*"))

(defn slow []
  (refresh)
  (run-all-tests #"integration\.nedap\.utils\.test\..*"))

(defn diff [x y]
  (-> x
      (lambdaisland.deep-diff/diff y)
      (lambdaisland.deep-diff/pretty-print)))