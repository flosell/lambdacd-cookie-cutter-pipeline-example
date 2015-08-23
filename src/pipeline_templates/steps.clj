(ns pipeline-templates.steps
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.core :as core]
            [lambdacd.steps.git :as git]))


(defn ^{:display-type :container} with-repo [repo-uri & steps]
  (fn [args ctx]
    (core/execute-steps steps (assoc args :repo repo-uri) ctx)))

(defn build [args ctx]
  (shell/bash ctx "/" (str "echo compiling x" (:repo args))))

(defn publish [args ctx]
  (shell/bash ctx "/" (str "echo publishing x" (:repo args))))