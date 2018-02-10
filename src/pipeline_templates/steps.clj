(ns pipeline-templates.steps
  (:require [lambdacd.steps.shell :as shell]
            [lambdacd-git.core :as git]))

(defn wait-for-commit-on-master [^:hide repo-uri]
  (fn [args ctx]
    (git/wait-for-git ctx repo-uri
                      ; how long to wait when polling. optional, defaults to 10000
                      :ms-between-polls 1000
                      ; which refs to react to. optional, defaults to refs/heads/master
                      :ref "refs/heads/master")))

(defn clone [^:hide repo-uri]
  (fn [args ctx]
    (git/clone ctx repo-uri (or (:revision ctx)
                            "master") (:cwd args))))

(defn ^{:display-type :step} run-tests [test-command]
  (fn [args ctx]
    (shell/bash ctx (:cwd args)
                (str "echo executing " test-command " on $(git remote get-url origin)"))))

(defn publish [args ctx]
  (shell/bash ctx "/" (str "echo publishing for repo " (:repo args))))
