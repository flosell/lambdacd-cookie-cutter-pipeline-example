(ns pipeline-templates.steps
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd.execution.core :as core]))


(defn ^{:display-type :container} with-repo [^:hide repo-uri & steps]
  (fn [args ctx]
    ; this is a mock 'with-repo' step that doesn't do any git stuff
    (core/execute-steps steps (assoc args :repo repo-uri) ctx)))

(defn ^{:display-type :step} run-tests [test-command]
  (fn [args ctx]
    (shell/bash ctx "/" (str "echo executing " test-command " on " (:repo args)))))

(defn publish [args ctx]
  (shell/bash ctx "/" (str "echo publishing for repo " (:repo args))))
