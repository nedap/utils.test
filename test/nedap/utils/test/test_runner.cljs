(ns nedap.utils.test.test-runner
  (:require
   [cljs.test :refer [run-tests] :as test]
   [cljs.nodejs :as nodejs]
   [nedap.utils.test.api :as utils.test]
   [unit.nedap.utils.test.api]
   [unit.nedap.utils.test.impl]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   (test/empty-env ::utils.test/exit-code-reporter)
   'unit.nedap.utils.test.api
   'unit.nedap.utils.test.impl))

(set! *main-cli-fn* -main)
