(ns nedap.utils.test.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [nedap.utils.test.api :refer-macros [run-tests]]
   [unit.nedap.utils.test.api]
   [unit.nedap.utils.test.impl]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'unit.nedap.utils.test.api
   'unit.nedap.utils.test.impl))

(set! *main-cli-fn* -main)
