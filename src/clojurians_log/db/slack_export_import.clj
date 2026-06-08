(ns clojurians-log.db.slack-export-import
  (:require
   [camel-snake-kebab.core :as csk]
   [charred.api :as charred]
   [clj-pgcopy.core :as pgcopy]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojurians-log.db :as db]
   [co.gaiwan.slack.raw-archive :as raw-archive]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

;; The slack export format is a zip with <channel-name>/<date>.json files. These
;; contain arrays with JSON objects representing messages. These are similar to
;; message in the conversations API or in events, but with some differences.
;; Notably there is no channel_id. There is a top-level channels.json with
;; channel and member information. Channels can change name, and old names can
;; be reused. We know channels.json is consistent with the directory names used
;; in the export, so we can use that to map channel_id, and then resolve the
;; channel in the db. We should not use names from the db to determine the
;; channel of a message.

(def staging->message-qry
  {:insert-into   [:message
                   {:select [[:channel.id  :channel_id]
                             [:member.id :member_id]
                             :text
                             :ts
                             [[:to_timestamp [:cast :ts :float]] :created_at]]
                    :from :message_staging
                    :join [:channel [:= :channel.slack_id :message_staging.channel_id]
                           :member [:= :member.slack_id :message_staging.member_id]]}]
   :columns       [:channel_id :member_id :text :ts :created_at]
   :on-conflict   [:channel_id :ts]
   :do-update-set {:fields [:member_id :text :ts :created_at]}})

(def staging->thread-qry
  {:update :message
   :set {:parent :pm.parent_id}
   :from
   [[{:select [[:s.ts :ts]
               [:ch.id :channel_id]
               [:m.id :parent_id]]
      :from [[:message_staging :s]]
      :where [:and
              [:!= nil :s.parent_ts]
              [:!= :s.ts :s.parent_ts]]
      :join [[:channel :ch]
             [:= :ch.slack_id :s.channel_id]
             [:message :m]
             [:and
              [:= :m.channel_id :ch.id]
              [:= :m.ts :s.parent_ts]]]}
     :pm]]
   :where [:and
           [:= :message.channel_id :pm.channel_id]
           [:= :message.ts :pm.ts]]})

(defn load-export-messages [export-dir]
  (let [known-channels (into
                        #{}
                        (map :slack-id)
                        (db/execute! ["SELECT slack_id FROM channel WHERE topic NOT LIKE '%noindex%' AND purpose NOT LIKE '%noindex%'"]))
        export-channels (with-open [rdr (io/reader (io/file export-dir "channels.json"))]
                          (charred/read-json rdr :key-fn csk/->kebab-case-keyword))]
    (eduction
     (comp
      (keep (fn [{:keys [id name]}]
              (when (known-channels id)
                [id name])))
      (mapcat (fn [[channel-id name]]
                (for [f (raw-archive/file-seq-by-exts (io/file export-dir name) [".json"])]
                  [channel-id f])))
      (mapcat (fn [[channel-id f]]
                (map (fn [msg]
                       (assoc msg "channel_id" channel-id))
                     (with-open [rdr (io/reader f)]
                       (charred/read-json rdr))))))
     export-channels)))

(defn import-messages! [export-dir]
  (let [messages (load-export-messages export-dir)
        cols [:channel_id :member_id :text :ts :parent_ts]]
    (jdbc/with-transaction [conn (db/conn)]
      (jdbc/execute! conn ["TRUNCATE message_staging"])
      (pgcopy/copy-into-table! conn
                               :message_staging
                               cols
                               (for [{:strs [type channel_id user text ts thread_ts]} messages
                                     :when (= "message" type)
                                     :when text
                                     :when (not (str/includes? text (str (char 0))))]
                                 [channel_id user text ts thread_ts]))

      (jdbc/execute! conn (sql/format staging->message-qry))
      (jdbc/execute! conn (sql/format staging->thread-qry)))))

(comment
  (db/execute! ["SELECT * FROM message LIMIT 10"])
  (db/execute! ["SELECT * FROM message_staging LIMIT 10"])
  (db/execute! ["UPDATE message SET parent = NULL"])
)
