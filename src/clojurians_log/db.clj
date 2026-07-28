(ns clojurians-log.db
  (:require
   [clojurians-log.system :as system]
   [hikari-cp.core :as hikari]
   [migratus.core :as migratus]
   [next.jdbc :as jdbc]
   [next.jdbc.date-time :as jdbc.date-time]))

(defn conn []
  (system/component :clojurians-log/db))

(defn get-migration-config
  ([]
   (get-migration-config (conn)))
  ([conn]
   {:store                :database
    :migration-dir        "migrations/"
    ;; :init-script          "init.sql"
    ;; :init-in-transaction? false
    :migration-table-name "migrations"
    :db                   {:datasource (jdbc/get-datasource conn)}}))

(defn execute! [& args]
  (apply jdbc/execute! (conn) args))

(defn init []
  (migratus/init (get-migration-config)))

(defn migrate-create [name]
  (migratus/create (get-migration-config) name))

(defn migrate
  ([]
   (migratus/migrate (get-migration-config)))
  ([migration-config]
   (migratus/migrate migration-config)))

(defn rollback
  "rollback the migration with the latest timestamp"
  []
  (migratus/rollback (get-migration-config)))

(defn migrate-up
  "bring up migrations matching the ids"
  [id]
  (migratus/up (get-migration-config) id))

(defn migrate-down
  "bring down migrations matching the ids"
  [id]
  (migratus/down (get-migration-config) id))

(defn component [{:hikari/keys [config] :as opts}]
  (let [data-source (hikari/make-datasource config)]
    (jdbc.date-time/read-as-instant)
    (doto (jdbc/with-options {:datasource data-source} jdbc/unqualified-snake-kebab-opts)
      migrate)))

(comment
  (rollback)
  (migrate)
  (migrate-create "add-indices")
  )
