(ns clojurians-log.db.queries
  "PostgreSQL database queries"
  (:require
   [clojure.core.memoize :as memo]
   [clojurians-log.db :as db]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(defn all-messages []
  (let [sqlmap {:select [:message.* :member.*]
                :from [:message]
                :join [:member [:= :message.member-id :member.id]] }
        query (sql/format sqlmap)]
    (db/execute! query {:builder-fn rs/as-kebab-maps})))

(defn single-message [channel-id ts]
  (let [sqlmap {:select [:message.*]
                :from [:message]
                :limit 1
                :where [:and
                        [:= :channel-id channel-id]
                        [:= :ts ts]]}
        query (sql/format sqlmap)]
    (first
     (db/execute! query {:builder-fn rs/as-kebab-maps}))))

(defn messages-by-channel-date [channel-id date]
  (let [sqlmap {:select [:message.* :member.*]
                :from [:message]
                :where [:and
                        [:is :parent nil]
                        [:= :message.channel-id channel-id]
                        [:raw (str "((message.created_at AT TIME ZONE 'UTC')::date) = '" date "'::date")]]
                :order-by [:message.id]
                :join [:member [:= :message.member-id :member.id]]}
        query (sql/format sqlmap)]
    (db/execute! query {:builder-fn rs/as-kebab-maps})))

(defn replies-for-messages [channel-id message-ids]
  (when (seq message-ids)
    (let [sqlmap {:select [:message.* :member.*]
                  :from [:message]
                  :where [:and
                          [:= :message.channel-id channel-id]
                          [:in :message.parent message-ids]]
                  ;; TODO: should sort based on ts instead of id
                  :order-by [:message.id]
                  :join [:member [:= :message.member-id :member.id]]}
          query (sql/format sqlmap)]
      (db/execute! query {:builder-fn rs/as-kebab-maps}))))

(defn reactions-for-messages [channel-id message-ids]
  (when (seq message-ids)
    (let [sqlmap {:select [[[:count :reaction.*]] :reaction.reaction :reaction.message-id]
                  :from [:reaction]
                  :where [:and
                          [:= :reaction.channel-id channel-id]
                          [:in :reaction.message-id message-ids]]
                  ;; TODO: should sort based on ts instead of id
                  :order-by [:reaction.message-id]
                  :group-by [:reaction.message-id :reaction.reaction]
                  }
          query (sql/format sqlmap)]
      (db/execute! query {:builder-fn rs/as-kebab-maps}))))

(defn channel-by-name [channel-name]
  (let [sqlmap {:select [:*]
                :from [:channel]
                :where [:= :name channel-name]}
        query (sql/format sqlmap)
        data (db/execute! query)]
    (first data)))

(defn channel-message-counts-by-date [channel-id]
  (let [sqlmap {:select [[[:count :*]]
                         [[:raw "(message.created_at AT TIME ZONE 'UTC')::date"] :created-at]]
                :from [:message]
                :limit 300
                :where [:and
                        [:= :channel-id channel-id]
                        [:<> :created-at nil]]
                :order-by [[[:raw "(message.created_at AT TIME ZONE 'UTC')::date"] :desc]]
                :group-by [[:raw "(message.created_at AT TIME ZONE 'UTC')::date"]]}
        data (db/execute! (sql/format sqlmap))]
    data))

(defn all-channels* []
  (let [sqlmap {:select [:channel.*]
                :from [:channel]
                :order-by [:channel.name]}]
    (db/execute! (sql/format sqlmap))))

(def all-channels (memo/ttl all-channels* :ttl/threshold 60000))

(defn member-cache-id-name* []
  (let [sqlmap {:select [:slack-id :name]
                :from [:member]}
        data (db/execute! (sql/format sqlmap))]
    (into {}
          (map (juxt :slack-id :name))
          data)))

(def member-cache-id-name (memo/ttl member-cache-id-name* :ttl/threshold 60000))

(defn search-messages-count [search-query]
  (let [sqlmap {:select [[[:count :*] :count]]
                :from [:message]
                :where [[:raw ["to_tsvector('english', text) @@ websearch_to_tsquery('english'," [:param :search-query] ")"]]]}
        query (sql/format sqlmap {:params {:search-query search-query}})
        data (db/execute! query {:builder-fn rs/as-kebab-maps})]
    (:count (first data))))

(defn search-messages [search-query]
  (let [sqlmap {:select [:message.* :member.*]
                :from [:message]
                :limit 200
                :join [:member [:= :message.member-id :member.id]]
                :order-by [[:created-at :desc]]
                :where [[:raw ["to_tsvector('english', text) @@ websearch_to_tsquery('english'," [:param :search-query] ")"]]]}
        query (sql/format sqlmap {:params {:search-query search-query}})
        data (db/execute! query {:builder-fn rs/as-kebab-maps})]
    data))

(defn chan-cache* []
  (let [sqlmap {:select [:id :name]
                :from [:channel]}
        data (db/execute! (sql/format sqlmap))]
    (into {}
          (map (juxt :name :id))
          data)))

(def chan-cache (memo/ttl chan-cache* :ttl/threshold 60000))

(defn chan-slack-id->id-cache* []
  (let [sqlmap {:select [:id :slack-id]
                :from [:channel]}
        data (db/execute! (sql/format sqlmap))]
    (into {}
          (map (juxt :slack-id :id))
          data)))

(def chan-slack-id->id-cache (memo/ttl chan-slack-id->id-cache* :ttl/threshold 60000))

(defn member-cache* []
  (let [sqlmap {:select [:id :slack-id]
                :from [:member]}
        data (db/execute! (sql/format sqlmap))]
    (into {}
          (map (juxt :slack-id :id))
          data)))

(def member-cache (memo/ttl member-cache* :ttl/threshold 30000))

(defn get-cache []
  {:chan-name->id (chan-cache)
   :chan-slack-id->id (chan-slack-id->id-cache)
   :member-slack->db-id (member-cache)})

(comment
  (def ds (user/ds))

  (single-message ds 2 "1652821554.591519")

  (replies-for-messages ds 79 [619])

  (reactions-for-messages ds 552 [227916])

  (member-cache-id-name ds)

  (take 10
        (all-messages ds))
  )
