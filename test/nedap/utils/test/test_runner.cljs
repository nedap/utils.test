(ns nedap.utils.test.test-runner
  (:require
   [cljs.nodejs :as nodejs]
   [integration.nedap.utils.test.matchers]
   [nedap.utils.test.api :refer-macros [run-tests]]
   [unit.nedap.utils.test.api]
   [unit.nedap.utils.test.impl]
   [unit.nedap.utils.test.matchers]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
   'integration.nedap.utils.test.matchers
   'unit.nedap.utils.test.api
   'unit.nedap.utils.test.impl
   'unit.nedap.utils.test.matchers))

(set! *main-cli-fn* -main)
