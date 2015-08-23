(ns pipeline-templates.steps
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.core :as core]
            [lambdacd.steps.git :as git]))


(defn ^{:display-type :container} with-repo [repo-uri & steps]
  (fn [args ctx]
    (core/execute-steps steps (assoc args :repo repo-uri) ctx)))

(defn ^{:display-type :step} run-tests [test-command]
  (fn [args ctx]
    (shell/bash ctx "/" (str "echo executing " test-command " on " (:repo args)))))

(defn publish [args ctx]
  (shell/bash ctx "/" (str "echo publishing for repo " (:repo args))))