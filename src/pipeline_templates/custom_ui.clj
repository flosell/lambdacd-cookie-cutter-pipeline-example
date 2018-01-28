(ns pipeline-templates.custom-ui
  (:require [compojure.route :as route]
            [lambdacd.ui.api :as api]
            [lambdacd.ui.ui-page :as ui-page]
            [compojure.core :refer [routes GET context]]
            [hickory.core :as hickory]
            [hickory.render :as hickory-render]
            [hickory.convert :as hickory-convert]
            [hickory.select :as hickory-select]
            [clojure.zip :as zip]))

(defn- navbar [projects]
  [:ul {:class "navbar"}
   (for [{pipeline-url :pipeline-url name :name} projects]
     (list
       [:li [:a {:href (str pipeline-url "/")} (str "| " name " |")]]))])

(defn- header [projects pipeline-name]
  [:div {:class "app__header"}
   [:a {:href "/"}
    [:h1 {:class "app__header__lambdacd"} "LambdaCD"]]
   (if pipeline-name
     [:span {:class "app__header__pipeline-name"} pipeline-name])
   (navbar projects)])

(defn- replace-title [hic pipeline-name]
  (let [locs (hickory-select/select-locs (hickory-select/tag :title) hic)]
    (if (empty? locs)
      (throw (Exception. "no locs found")))
    (zip/replace (first locs) {:type :element :tag :title :attrs nil :content [(str pipeline-name " - LambdaCD")]})))

(defn- append-style [hic style]
  (let [locs (hickory-select/select-locs (hickory-select/tag :head) hic)]
    (if (empty? locs)
      (throw (Exception. "no locs found")))
    (zip/append-child (first locs) (first (hickory-convert/hiccup-fragment-to-hickory [[:style style]])))))

(defn- replace-header [hic header]
  (let [locs (hickory-select/select-locs (hickory-select/class "app__header") hic)]
    (if (empty? locs)
      (throw (Exception. "no locs found")))
    (zip/replace (first locs) (first (hickory-convert/hiccup-fragment-to-hickory [header])))))

(defn new-ui-page [projects pipeline]
  (let [pipeline-name  (get-in pipeline [:context :config :name])]
    (-> (ui-page/ui-page pipeline)
        (hickory/parse)
        (hickory/as-hickory)
        (replace-title pipeline-name)
        (zip/root)
        (append-style ".navbar li { display: inline; padding-right: 20px; }")
        (zip/root)
        (replace-header (header projects pipeline-name))
        (zip/root)
        (hickory-render/hickory-to-html))))

(defn ui-for
  ([pipeline projects]
   (routes
     (context "/api" [] (api/rest-api pipeline))
     (GET "/" [] (new-ui-page projects pipeline))
     (route/resources "/" {:root "public"}))))

