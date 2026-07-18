(ns clojurians-log.slack-unix-socket
  "Component that reads Slack JSON events from a unix domain socket

  The socket is written to by slack-event-sink, each message is a JSON encoded
  Slack event, which gets imported via [[slack-import/from-event]].

  If the socket is missing or the connection drops (e.g. slack-event-sink
  restarts), we keep retrying every `:retry-ms` (config: `:slack/unix-socket-retry-ms`)."
  (:require
   [charred.api :as charred]
   [clojurians-log.db.queries :as queries]
   [clojurians-log.db.slack-import :as slack-import]
   [lambdaisland.glogc :as log])
  (:import
   (java.net UnixDomainSocketAddress)
   (java.nio.channels Channels ClosedChannelException SocketChannel)))

(defn connect
  "Connect to the unix domain socket at `path`, returns a SocketChannel, or nil
  if the socket does not exist or can't be opened."
  [path]
  (SocketChannel/open (UnixDomainSocketAddress/of (str path))))

(defn import-event
  "Import a single Slack event (map with string keys)"
  [event]
  (try
    (slack-import/from-event event (queries/get-cache))
    (catch Exception e
      (log/error :slack-unix-socket/import-failed {:event event}
                 :exception e))))

(defn read-loop
  "Read consecutive JSON values off the channel, calling `handle-event` on each.
  Returns when the channel is closed or EOF is reached."
  [^SocketChannel channel]
  (try
    (with-open [events (charred/read-json-supplier
                        (Channels/newInputStream channel)
                        {:eof-error? false
                         :eof-value nil})]
      (run! import-event events))
    (catch ClosedChannelException _)
    (catch Exception e
      (log/warn :slack-unix-socket/read-loop-aborted {}
                :exception e))))

(defn run-loop
  "Connect to the socket and read events off of it, reconnecting whenever the
  connection is lost or can't be established. The current connection is kept in
  the `channel` atom, so it can be closed on stop. Repeated connection failures
  only warn once."
  [{:keys [socket-path retry-ms channel]}]
  (try
    (loop [warned? false]
      (let [conn (try
                   (connect socket-path)
                   (catch Exception e
                     (when-not warned?
                       (log/warn :slack-unix-socket/connect-failed
                                 {:path socket-path
                                  :retry-ms retry-ms}
                                 :exception e))
                     nil))]
        (when conn
          (reset! channel conn)
          (log/info :slack-unix-socket/connected {:path socket-path})
          (read-loop conn)
          (log/warn :slack-unix-socket/disconnected {:path socket-path
                                                     :retry-ms retry-ms}))
        (Thread/sleep ^long retry-ms)
        (recur (nil? conn))))
    (catch InterruptedException _)))

(def component
  {:start
   (fn [{:keys [socket-path retry-ms] :as opts}]
     (when socket-path
       (future
         (-> opts
             (assoc :retry-ms retry-ms
                    :channel  (atom nil))
             run-loop))))})
