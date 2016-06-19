(ns pipeline-templates.custom-ui
  (:require [compojure.route :as route]
            [lambdacd.ui.api :as api]
            [lambdacd.ui.ui-page :as ui-page]
            [compojure.core :refer [routes GET context]]
            [hiccup.core :as h]))

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

(defn- ui-page [projects pipeline]
  (let [pipeline-name (get-in pipeline [:context :config :name])
        ui-config-data (get-in pipeline [:context :config :ui-config])]
    (h/html
      [:html
       [:head
        [:title (str pipeline-name " - LambdaCD")]
        (ui-page/favicon)
        (ui-page/css-includes)
        (ui-page/ui-config ui-config-data)
        [:style ".navbar li { display: inline; padding-right: 20px; }"]]
       [:body
        [:div {:class "app l-horizontal"}
         (header projects pipeline-name)
         (ui-page/app-placeholder)]
        (ui-page/js-includes)]])))

(defn ui-for
  ([pipeline projects]
   (routes
     (context "/api" [] (api/rest-api pipeline))
     (GET "/" [] (ui-page projects pipeline))
     (route/resources "/" {:root "public"}))))

