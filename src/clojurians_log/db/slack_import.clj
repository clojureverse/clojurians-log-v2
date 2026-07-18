(ns clojurians-log.db.slack-import
  (:require
   [clojurians-log.db :as db]
   [clojurians-log.db.import :as import]
   [honey.sql :as sql]
   [lambdaisland.glogc :as log]))

(defmulti from-event
  "Expects slack socket live events as maps with string keys"
  (fn [{:strs [type subtype]} cache]
    [type subtype]))

(defmethod from-event ["message" nil]
  [event cache]
  (let [query (-> event
                  (import/event->tx cache))
        sql-query (sql/format query)]
    #_(println sql-query)
    (db/execute! sql-query)))

(defmethod from-event ["message" "message_changed"]
  [{:strs [message channel] :as event} cache]
  (-> message
      (assoc "channel" channel)
      (from-event cache)))

(defmethod from-event ["message" "message_deleted"]
  [event cache]
  (let [query (-> event
                  (import/message-tombstone->tx cache))
        sql-query (sql/format query)]
    (db/execute! sql-query)))

(defmethod from-event ["message" "tombstone"]
  [event cache]
  (let [query (-> event
                  (import/message-tombstone->tx cache))
        sql-query (sql/format query)]
    (db/execute! sql-query)))

(defmethod from-event ["reaction_added" nil]
  [event cache]
  (let [query (import/reaction->tx event cache)]
    (db/execute! (sql/format query))))

(defmethod from-event ["reaction_removed" nil]
  [event cache]
  (let [query (-> event
                  (import/reaction-removed->tx cache))]
    (db/execute! (sql/format query))))

(defmethod from-event ["channel_created" nil]
  [event cache]
  (let [query (-> event
                  (import/channel->tx cache))]
    (db/execute! (sql/format query))))

(defmethod from-event ["channel_rename" nil]
  [event cache]
  (-> event
      (assoc "type" "channel_created")
      (from-event cache)))

(defmethod from-event :default
  [{:strs [type subtype]} cache]
  (log/warn :event/not-handled [type subtype]
            :message "Event import not handled"))
