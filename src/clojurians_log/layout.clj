(ns clojurians-log.layout
  (:require [lambdaisland.ornament :as o]
            [clojurians-log.styles :as styles]
            [clojure.java.io :as io]))

(defn base
  ([body]
   (base nil body))
  ([extra-head body]
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:content "width=device-width, initial-scale=1" :name "viewport"}]
     [:link {:rel "stylesheet" :href "/assets/fonts/inter.css"}]
     (if (io/resource "public/css/compiled/ornament.css")
       [:link {:rel "stylesheet" :href "/assets/css/compiled/ornament.css"}]
       [:link {:rel "stylesheet" :href "/styles.css"}])
     extra-head]
    [:body
     [:div#app
      body]
     [:script {:type "application/javascript" :src (str "/assets/js/main.js")}]]]))
