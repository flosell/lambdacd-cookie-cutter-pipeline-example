(ns pipeline-templates.pipeline
  (:use [lambdacd.steps.control-flow]
        [lambdacd.steps.manualtrigger]
        [pipeline-templates.steps])
  (:require
        [ring.server.standalone :as ring-server]
        [lambdacd.ui.ui-server :as ui]
        [pipeline-templates.custom-ui :as custom-ui]
        [lambdacd.runners :as runners]
        [lambdacd.util :as util]
        [lambdacd.core :as lambdacd]
        [compojure.core :as compojure]
        [hiccup.core :as h]
        [clojure.tools.logging :as log])
  (:gen-class))

(def projects [{:name         "LambdaCD"
                :pipeline-url "/lambdacd"
                :repo-uri     "git@github.com:flosell/lambdacd.git"
                :test-command "./go test"}
               {:name         "LambdaCD Artifacts"
                :pipeline-url "/artifacts"
                :repo-uri     "git@github.com:flosell/lambdacd-artifacts.git"
                :test-command "./go test"}
               {:name         "LambdaCD Leiningen Template"
                :pipeline-url "/template"
                :repo-uri     "git@github.com:flosell/lambdacd-template.git"
                :test-command "lein test"}])

(defn mk-pipeline-def [{repo-uri :repo-uri test-command :test-command}]
  `(
     wait-for-manual-trigger
     (with-repo ~repo-uri
                (run-tests ~test-command)
                publish)))

(defn pipeline-for [project]
  (let [home-dir     (util/create-temp-dir)
        config       { :home-dir home-dir :name (:name project)}
        pipeline-def (mk-pipeline-def project)
        pipeline     (lambdacd/assemble-pipeline pipeline-def config)
        ui-config    {:expand-active-default true
                      :expand-failures-default true}
        app          (custom-ui/ui-for pipeline projects)]
    (runners/start-one-run-after-another pipeline)
    app))

(defn mk-context [project]
  (let [app (pipeline-for project)] ; don't inline this, otherwise compojure will always re-initialize a pipeline on each HTTP request
    (compojure/context (:pipeline-url project) [] app)))


;; Nice overview page:
(defn mk-link [{url :pipeline-url name :name}]
  [:li [:a {:href (str url "/")} name]])

(defn mk-index [projects]
  (h/html
    [:html
     [:head
      [:title "Pipelines"]]
     [:body
      [:h1 "Pipelines:"]
      [:ul (map mk-link projects)]]]))

(defn -main [& args]
  (let [
        contexts (map mk-context projects)
        routes (apply compojure/routes
                      (conj contexts (compojure/GET "/" [] (mk-index projects))))]
       (ring-server/serve routes {:open-browser? false
                               :port 8080})))
