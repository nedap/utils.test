(ns nedap.utils.test.test-runner
 (:require
  [cljs.nodejs :as nodejs]
  [cljs.test :refer-macros [run-tests]]
  [unit.nedap.utils.test.api]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.utils.test.api))

(set! *main-cli-fn* -main)
