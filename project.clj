(defproject com.nedap.staffing-solutions/utils.test "1.3.0"
  ;; Please keep the dependencies sorted a-z.
  :dependencies [[org.clojure/clojure "1.10.0"]]

  :description "utils.test"

  :url "http://github.com/nedap/utils.test"

  :min-lein-version "2.0.0"

  :license {:name "EPL-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :signing {:gpg-key "releases-staffingsolutions@nedap.com"}

  :repositories {"releases" {:url      "https://nedap.jfrog.io/nedap/staffing-solutions/"
                             :username :env/artifactory_user
                             :password :env/artifactory_pass}}

  :deploy-repositories {"clojars" {:url      "https://clojars.org/repo"
                                   :username :env/clojars_user
                                   :password :env/clojars_pass}}

  :repository-auth {#"https://nedap.jfrog\.io/nedap/staffing-solutions/"
                    {:username :env/artifactory_user
                     :password :env/artifactory_pass}}

  :target-path "target/%s"

  :repl-options {:init-ns dev}

  :monkeypatch-clojure-test false

  :plugins [[lein-cljsbuild "1.1.7"]]

  ;; Please don't add `:hooks [leiningen.cljsbuild]`. It can silently skip running the JS suite on `lein test`.

  :cljsbuild {:builds {"test" {:source-paths ["src" "test"]
                               :compiler     {:main          nedap.utils.test.test-runner
                                              :output-to     "target/out/tests.js"
                                              :output-dir    "target/out"
                                              :target        :nodejs
                                              :optimizations :none}}}}

  :profiles {:dev      {:dependencies [[cider/cider-nrepl "0.16.0" #_"formatting-stack needs it"]
                                       [com.clojure-goes-fast/clj-java-decompiler "0.2.1"]
                                       [criterium "0.4.4"]
                                       [formatting-stack "0.18.3"]
                                       [lambdaisland/deep-diff "0.0-29"]
                                       [org.clojure/tools.namespace "0.3.0-alpha4"]
                                       [org.clojure/tools.reader "1.1.1" #_"formatting-stack needs it"]]
                        :source-paths ["dev" "test"]}

             :provided {:dependencies [[org.clojure/clojurescript "1.10.520"]]}})
