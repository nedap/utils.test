(defproject com.nedap.staffing-solutions/utils.test "0.1.0-alpha1"
  ;; Please keep the dependencies sorted a-z.
  :dependencies [[com.nedap.staffing-solutions/utils.modular "0.3.0"]
                 [com.nedap.staffing-solutions/utils.spec "0.6.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/test.check "0.10.0-alpha3"]]

  :description "utils.test"

  :url "http://github.com/nedap/utils.test"

  :min-lein-version "2.0.0"

  :license {:name "EPL-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :signing {:gpg-key "servicedesk-PEP@nedap.com"}

  :repositories {"releases" {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                             :username :env/artifactory_user
                             :password :env/artifactory_pass}}

  :deploy-repositories [["releases" {:url "https://nedap.jfrog.io/nedap/staffing-solutions/"}]]

  :repository-auth {#"https://nedap.jfrog\.io/nedap/staffing-solutions/"
                    {:username :env/artifactory_user
                     :password :env/artifactory_pass}}

  :target-path "target/%s"

  :plugins [[lein-cljsbuild "1.1.7"]]

  ;; Please don't add `:hooks [leiningen.cljsbuild]`. It can silently skip running the JS suite on `lein test`.
  ;; It also interferes with Cloverage.

  :cljsbuild {:builds {"test" {:source-paths ["src" "test"]
                               :compiler     {:main          nedap.utils.test.test-runner
                                              :output-to     "target/out/tests.js"
                                              :output-dir    "target/out"
                                              :target        :nodejs
                                              :optimizations :none}}}}

  :profiles {:dev      {:plugins [[lein-cloverage "1.0.13"]]}

             :provided {:dependencies [[org.clojure/clojurescript "1.10.520"]]}})
