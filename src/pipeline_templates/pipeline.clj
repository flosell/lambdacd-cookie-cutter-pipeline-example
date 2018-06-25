(ns pipeline-templates.pipeline
  (:use [lambdacd.steps.control-flow]
        [lambdacd.steps.manualtrigger]
        [pipeline-templates.steps])
  (:require
    [org.httpkit.server :as http-kit]
    [pipeline-templates.custom-ui :as custom-ui]
    [lambdacd-git.core :as git]
    [lambdacd.runners :as runners]
    [lambdacd.core :as lambdacd]
    [lambdaui.core :as lambdaui]
    [compojure.core :as compojure]
    [hiccup.core :as h])
  (:gen-class)
  (:import (java.nio.file.attribute FileAttribute)
           (java.nio.file Files)))

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
     (either
       wait-for-manual-trigger
       (wait-for-commit-on-master ~repo-uri))
     (with-workspace
       (clone ~repo-uri)
       (run-tests ~test-command)
       publish)))

(defn- mk-lambda-ui-links [projects]
  (for [{pipeline-url :pipeline-url name :name} projects]
    {:url (str pipeline-url "/lambda-ui/lambdaui") :text name}))

(defn pipeline-for [project]
  (let [home-dir     (str (Files/createTempDirectory "lambdacd" (into-array FileAttribute [])))
        config       {:home-dir home-dir :name (:name project)
                      :ui-config {:navbar {:links (mk-lambda-ui-links projects)}}}
        pipeline-def (mk-pipeline-def project)
        pipeline     (lambdacd/assemble-pipeline pipeline-def config)
        app          (compojure/routes
                       (compojure/context "/lambda-ui" [] (lambdaui/ui-for pipeline :contextPath (str (:pipeline-url project) "/" "lambda-ui")))
                       (compojure/context "/reference-ui" [] (custom-ui/ui-for pipeline projects))
                       (git/notifications-for pipeline))]
    (runners/start-one-run-after-another pipeline)
    app))

(defn mk-context [project]
  (let [app (pipeline-for project)] ; don't inline this, otherwise compojure will always re-initialize a pipeline on each HTTP request
    (compojure/context (:pipeline-url project) [] app)))


;; Nice overview page:
(defn mk-link [{url :pipeline-url name :name}]
  [:li [:span
        [:a {:href (str url "/reference-ui/")} name]
        " "
        [:a {:href (str url "/lambda-ui/lambdaui")} "(LambdaUI)"]]])

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
       (http-kit/run-server routes {:port 8080})))
