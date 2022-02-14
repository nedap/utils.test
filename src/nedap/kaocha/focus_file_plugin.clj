(ns nedap.kaocha.focus-file-plugin
  "A plugin adding `focus-file` to focus on filename instead of ns."
  (:require
   [kaocha.plugin :as p]))

(defn- accumulate [m k v]
  (update m k (comp vec distinct (fnil conj [])) v))

(p/defplugin nedap.kaocha/focus-file-plugin
  (cli-options [opts]
    (conj opts [nil "--focus-file FILE" "Only run tests in this file, skip others." :assoc-fn accumulate]))

  (config [{{:keys [focus-file]} :kaocha/cli-options
            :as config}]
    (if (seq focus-file)
      (-> config
          (update :kaocha/tests #(mapv (fn [x] (assoc x :kaocha/test-paths focus-file)) %))
          (update :kaocha/cli-options dissoc :focus-file))
     config)))
