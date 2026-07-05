(ns clojurians-log.layout
  (:require
   [clojure.java.io :as io]
   [clojurians-log.assets :as assets]
   [clojurians-log.styles :as styles]
   [lambdaisland.ornament :as o]))

(defn base
  ([body]
   (base nil body))
  ([extra-head body]
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:content "width=device-width, initial-scale=1" :name "viewport"}]
     (for [f assets/css]
       [:link {:rel "stylesheet" :href (str "/" f)}])
     extra-head]
    [:body
     [:div#app
      body]
     (for [f assets/js]
       [:script {:type "application/javascript" :src (str "/" f)}])]]))
