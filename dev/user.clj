(ns user)

(defmacro jit [sym]
  `(requiring-resolve '~sym))

(defn go
  ([]
   ((jit clojurians-log.system/start!)))
  ([& ks]
   (apply (jit clojurians-log.system/start!) ks)))

(defn browse []
  ((jit clojure.java.browse/browse-url)
   (str "http://localhost:"
        ((jit clojurians-log.config/get) :http/port))))
