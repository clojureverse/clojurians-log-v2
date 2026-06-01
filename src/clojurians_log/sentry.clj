(ns clojurians-log.sentry
  (:import
   (io.sentry Sentry)))

(defn component [{:keys [dsn debug? trace-sample-rate]}]
  (when dsn
    (.init Sentry (fn [opts]
                    (doto opts
                      (.setDsn dsn)
                      (.setTracesSampleRate trace-sample-rate)
                      (.setDebug debug?))))))
