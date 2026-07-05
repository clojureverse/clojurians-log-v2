(ns clojurians-log.styles
  "Fixed styles and style compilation logic"
  (:require
   [lambdaisland.ornament :as o]))

(o/defrules global-styles
  [:html
   {:font-family "'Inter', sans-serif"
    :-webkit-font-smoothing "antialiased"
    :-moz-osx-font-smoothing "grayscale"}]
  [:body {:margin 0 :padding 0}]
  [:pre
   :bg-gray-200 :text-gray-900 :text-sm :font-mono :rounded :p-3 :mt-2 :whitespace-pre :overflow-scroll
   {:border "1px solid #f3f4f6"}]
  [:code :bg-gray-200]
  [:a :text-blue-500]
  [:.slack-message__reaction
   :inline-flex
   {:line-height "16px"
    :margin-right "4px"
    :border "none"
    :vertical-align "top"
    :align-items "center"
    :padding "4px 6px"
    :margin-bottom "4px"
    :font-size "11px"
    :border-radius "12px"
    :background "#f6f6f6"}]
  [:.slack-message__reaction [:img {:max-width "16px"}]]
  [:.emoji {:font-style "normal"}]
  [:.user-mention :inline-block :bg-blue-100 :text-blue-800 :no-underline]
  [:.user-mention [:a {:text-decoration "none" :color "inherit"}]]
  )

(defn spit-styles []
  (require 'clojurians-log.http) ;; make sure all components are loaded
  (let [styles (o/defined-styles {:compress? false})]
    (println "Writing assets/ornament.css" (alength (.getBytes ^String styles)) "bytes")
    (spit "assets/ornament.css" styles)))

(defn component [{:keys [precompile]}]
  (when precompile (spit-styles)))
