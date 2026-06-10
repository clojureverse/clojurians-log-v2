(ns clojurians-log.db.import
  (:require
   [clojure.string :as string]
   [lambdaisland.glogc :as log]
   [clojurians-log.db.queries :as queries]
   [clojurians-log.time-utils :as time-utils]
   [clojurians-log.utils :as utils]))

(defmulti event->tx
  (fn [{:strs [type subtype]} cache]
    [type subtype]))

(defmethod event->tx :default [_ _]
  ;; return nil by default, this will let us skip events we don't (yet) care
  ;; about
  nil)

(defn message->tx [{:strs [channel user text ts thread_ts] :as message}
                   {:keys [member-slack->db-id chan-slack-id->id] :as cache}]
  (let [member-id (get member-slack->db-id user)
        channel-id (get chan-slack-id->id channel)
        _ (log/debug :user user :member-id member-id :channel-id channel-id)
        parent-id (when (and thread_ts (not= thread_ts ts))
                    {:select [:id]
                     :from [:message]
                     :limit 1
                     :where [:and
                             [:= :ts thread_ts]
                             [:= :channel-id channel-id]]})
        value {:channel-id channel-id
               :member-id member-id
               :text (string/replace text #"\u0000" "")
               :ts ts
               :created-at (time-utils/ts->inst ts)
               :parent parent-id
               :deleted-ts nil}]
    {:insert-into [:message]
     :values [value]
     ;;:on-conflict []
     :on-conflict {:on-constraint :message_channel_id_ts_key}
     :do-update-set {:fields [:text :channel-id]}
     ;;:do-nothing true
     :returning [:ts :id]}))

(defmethod event->tx ["message" nil] [message cache]
  (message->tx message cache))

(defmethod event->tx ["message" "message_deleted"] [{:strs [deleted_ts channel] :as message} cache]
  nil)

(defmethod event->tx ["message" "message_changed"] [{:keys [message channel]} cache]
  #_(event->tx (assoc message :channel channel)))

(defmethod event->tx ["message" "thread_broadcast"] [message cache]
  nil
  #_(assoc
      (message->tx message) :message/thread-broadcast? true))

;; Can't do this, or we'll get DB errors when deleting the top of a thread
#_(defn message-deleted->tx [{:strs [deleted_ts channel]}
                             {:keys [chan-slack-id->id] :as cache}]
    (let [channel-id (get chan-slack-id->id channel)]
      {:delete []
       :from [:message]
       :where [:and
               [:= :channel-id channel-id]
               [:= :ts deleted_ts]]}))

(def logbot2-user-id "D0B4HMZMS4X")

(defn message-tombstone->tx [{:strs [ts channel]}
                             {:keys [member-slack->db-id chan-slack-id->id] :as cache}]
  (let [channel-id (get chan-slack-id->id channel)
        member-id (get member-slack->db-id logbot2-user-id)]
    {:update :message
     :set {:text "This message was deleted."
           :member-id member-id
           :deleted-ts ts}
     :where [:and
             [:= :channel-id channel-id]
             [:= :ts ts]]}))

(defn channel->tx [{:strs [id name_normalized name]} cachs]
  {:insert-into [:channel]
   :values {:slack-id id
            :name (or name_normalized name)}
   :on-conflict :slack-id
   :do-update-set {:fields [:name]}})

(defn reaction-removed->tx [{:strs [item user reaction] :as event}
                            {:keys [member-slack->db-id
                                    chan-slack-id->id] :as cache}]
  (prn event)
  (let [{:strs [channel ts]} item
        channel-id (get chan-slack-id->id channel)
        member-id (get member-slack->db-id user)]
    (prn channel channel-id (type chan-slack-id->id))
    {:delete []
     :from [:reaction]
     :where [:and
             [:= :channel-id channel-id]
             [:= :member-id member-id]
             [:= :message-id {:select [:id]
                              :from [:message]
                              :limit 1
                              :where [:and
                                      [:= :ts ts]
                                      [:= :channel-id channel-id]]}]
             [:= :reaction reaction]]}))

(defn reaction->tx [{:strs [item user reaction]}
                    {:keys [member-slack->db-id
                            chan-slack-id->id] :as cache}]
  (let [{:strs [channel ts]} item
        channel-id (get chan-slack-id->id channel)
        member-id (get member-slack->db-id user)]
    {:insert-into [:reaction]
     :values [{:channel-id channel-id
               :member-id member-id
               :message-id {:select [:id]
                            :from [:message]
                            :limit 1
                            :where [:and
                                    [:= :ts ts]
                                    [:= :channel-id channel-id]]}
               :reaction reaction}]
     :on-conflict {:on-constraint :reaction_member_id_channel_id_message_id_reaction_key}
     :do-nothing true}))

(defn reactions->tx [{:strs [channel-id user reactions ts thread_ts] :as message}
                     {:keys [member-slack->db-id message-ts->db-id] :as cache}]
  (let [member-id (get member-slack->db-id user)
        message-id (get message-ts->db-id ts)]
    (mapcat (fn reaction-val [reaction-entry]
              (map (fn reaction-for-each-user [user]
                     {:channel-id channel-id
                      :member-id (get member-slack->db-id user)
                      :message-id message-id
                      :reaction (:name reaction-entry)})
                   (:users reaction-entry)))
            reactions)))
