(ns clojurians-log.db
  (:require
   [clojurians-log.system :as system]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.date-time :as jdbc.date-time]))

(defn conn []
  (system/component :clojurians-log.db))

(defn get-migration-config [conn]
  {:store                :database
   :migration-dir        "migrations/"
   ;; :init-script          "init.sql"
   ;; :init-in-transaction? false
   :migration-table-name "migrations"
   :db                   {:datasource (jdbc/get-datasource conn)}})

(defn execute! [& args]
  (apply jdbc/execute! (conn) args))

(defn init []
  (migratus/init (get-migration-config)))

(defn migrate-create [name]
  (migratus/create (get-migration-config) name))

(defn migrate [conn]
  (migratus/migrate (get-migration-config conn)))

(defn migrate-rollback []
  "rollback the migration with the latest timestamp"
  (migratus/rollback (get-migration-config)))

(defn migrate-up [id]
  "bring up migrations matching the ids"
  (migratus/up (get-migration-config) 20111206154000))

(defn migrate-down [id]
  "bring down migrations matching the ids"
  (migratus/down (get-migration-config) 20111206154000))

(defn component [{:keys [type user port password name]}]
  (prn "HERE2")
  ;; TODO: add connection pooling
  (let [data-source (jdbc/get-datasource {:dbtype type
                                          :user user
                                          :port port
                                          :password password
                                          :dbname name
                                          :serverTimezone "UTC"})]
    (jdbc.date-time/read-as-instant)
    (doto (jdbc/with-options data-source jdbc/unqualified-snake-kebab-opts)
      migrate)))

(comment
  (migrate)
  )
