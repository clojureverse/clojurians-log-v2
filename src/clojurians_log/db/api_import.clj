(ns clojurians-log.db.api-import
  "Import users/channels directly from the Slack API"
  (:require
   [clj-pgcopy.core :as pgcopy]
   [clojure.string :as str]
   [clojurians-log.config :as config]
   [clojurians-log.db :as db]
   [co.gaiwan.slack.api :as slack]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(defn import-users! []
  (let [conn  (slack/conn (config/get :slack/bot-token))
        users (slack/users conn)
        cols  [:slack_id :name :display_name :image_192]]
    (println "Importing users from API")
    (jdbc/with-transaction [conn (db/conn)]
      (println "  Truncate staging table...")
      (jdbc/execute! conn ["TRUNCATE member_staging"])
      (println "  Load staging table...")
      (pgcopy/copy-into-table! conn
                               :member_staging
                               cols
                               (for [{:user/keys [id name] :user-profile/keys [display-name image-192]} users]
                                 [id name display-name image-192]))
      (println "  Copying into member table...")
      (jdbc/execute!
       conn
       (sql/format
        {:insert-into   [:member {:select cols :from :member_staging}]
         :columns       cols
         :on-conflict   :slack-id
         :do-update-set {:fields (vec (next cols))}
         :returning     [:slack-id :name]}))
      (println "   User import finished. Yay!"))))

(defn import-channels! []
  (let [conn     (slack/conn (config/get :slack/bot-token))
        channels (slack/user-conversations conn)
        cols     [:slack_id :name :purpose :topic]]
    (println "Importing channels from API")
    (jdbc/with-transaction [conn (db/conn)]
      (println "  Truncate staging table...")
      (jdbc/execute! conn ["TRUNCATE channel_staging"])
      (println "  Load staging table...")
      (pgcopy/copy-into-table! conn
                               :channel_staging
                               cols
                               (for [{:channel/keys [id name purpose topic] :as channel} channels
                                     :when (not
                                            (or
                                             (and purpose (str/includes? purpose "noindex"))
                                             (and topic (str/includes? topic "noindex"))))]
                                 (do
                                   (when (not (and purpose topic))
                                     (println "PURPOSE/TOPIC MISSING:" channel))
                                   [id name purpose topic])))
      (println "  Copying into channel table...")
      (jdbc/execute!
       conn
       (sql/format
        {:insert-into   [:channel {:select cols :from :channel_staging}]
         :columns       cols
         :on-conflict   :slack-id
         :do-update-set {:fields (vec (next cols))}
         :returning     [:slack-id :name]}))
      (println "   Channel import finished. Yay!"))))
