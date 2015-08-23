(ns pipeline-templates.pipeline
  (:use [lambdacd.steps.control-flow]
        [lambdacd.steps.manualtrigger]
        [pipeline-templates.steps])
  (:require
        [ring.server.standalone :as ring-server]
        [lambdacd.ui.ui-server :as ui]
        [lambdacd.runners :as runners]
        [lambdacd.util :as util]
        [lambdacd.core :as lambdacd]
        [compojure.core :as compojure]
        [clojure.tools.logging :as log])
  (:gen-class))



(defn mk-pipeline-def [repo-uri]
  `(
     wait-for-manual-trigger
     (with-repo ~repo-uri
                build
                publish)))



(defn foo [repo-uri]
  (let [home-dir (util/create-temp-dir)
        config { :home-dir home-dir :dont-wait-for-completion false}
        pipeline (lambdacd/assemble-pipeline (mk-pipeline-def repo-uri) config)
        app      (ui/ui-for pipeline)]
    (runners/start-one-run-after-another pipeline)
  app))


(defn -main [& args]
  (let [app (foo "some-repo")
        app2 (foo "some-other-repo")
        routes (apply compojure/routes
                 [(compojure/context "/some"       [] app)
                 (compojure/context "/some-other" [] app2)])]
       (ring-server/serve routes {:open-browser? false
                               :port 8080})))
