(ns clojurians-log.http
  (:require
   [clj-simple-stats.core :as simple-stats]
   [clojurians-log.assets :as assets]
   [clojurians-log.config :as config]
   [clojurians-log.routes :as routes]
   [io.pedestal.log :as log]
   [lambdaisland.hiccup :as hiccup]
   [muuntaja.core :as m]
   [muuntaja.format.core :as muuntaja-format]
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja-middleware]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.adapter.jetty :as jetty])
  (:import
   (java.io OutputStream)))

(defn html-encoder
  "Muuntaja encoder that renders HTML
  Expects a Clojure collection with a `:view-fn` in the metadata, which takes
  the `:body` collection as argument, and returns the body as a string."
  [opts]
  (reify muuntaja-format/EncodeToBytes
    (encode-to-bytes [_ data charset]
      (let [view (get (meta data) :view-fn)
            rendered (view data)]
        (.getBytes ^String rendered ^String charset)))
    muuntaja-format/EncodeToOutputStream
    (encode-to-output-stream [_ data charset]
      (fn [^OutputStream output-stream]
        (let [view (get (meta data) :view-fn)
              rendered (view data)]
          (.write output-stream (.getBytes ^String rendered ^String charset)))))))

(defn muuntaja-instance
  "Create a Muuntaja instance that includes HTML handling
  Can take options just like [[muuntaja.core/create]],
  see [[muuntaja.core/default-options]]."
  ([]
   (muuntaja-instance m/default-options))
  ([opts]
   (m/create
    (-> opts
        (assoc :default-format "text/html")
        (assoc-in [:formats "text/html"]
                  (muuntaja-format/map->Format
                   {:name :html
                    :encoder [html-encoder]}))))))

(defn view-fn-middleware [handler]
  (fn [request]
    (let [resp (handler request)]
      (if-let [view (get resp :view)]
        (update resp :body vary-meta assoc :view-fn
                (comp hiccup/render view))
        resp))))

(defn app []
  (ring/ring-handler
   (ring/router
    (into
     (assets/routes)
     (routes/routes))
    {:conflicts nil
     :data {:muuntaja   (muuntaja-instance)
            :middleware [muuntaja-middleware/format-middleware
                         parameters/parameters-middleware
                         view-fn-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler {:method :add})
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "Page not found."})}))
   {:middleware [simple-stats/wrap-stats]}))

(def component
  {:start
   (fn [{:keys [port]}]
     (log/info :jetty/starting {:port port})
     (jetty/run-jetty (if (config/get :app/rebuild-router-on-each-request)
                        #((app) %)
                        (app))
                      {:port (Long. port)
                       :join? false}))
   :stop
   (fn [server]
     (.stop server))})
