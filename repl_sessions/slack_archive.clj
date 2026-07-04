(ns repl-sessions.slack-archive
  (:require
   [clojurians-log.config :as config]
   [clojurians-log.db.queries :as qry]
   [clojurians-log.db.slack-import :as si]
   [co.gaiwan.slack.archive :as archive]
   [co.gaiwan.slack.normalize :as normalize]
   [co.gaiwan.slack.raw-archive :as raw]))

(def archive-dir "/home/arne/Clojurians-Log/archive")

(def raw-events (raw/dir-event-seq archive-dir))

(let [cache (qry/get-cache)]
  (run! #(si/from-event % cache) raw-events))



(time
 (def arch (archive/raw->archive cljians-log-dir (archive/archive "/tmp/cljians-archive"))))

;;=> "Elapsed time: 33486.306643 msecs"



;; (def archive (archive/fetch-api-resources arch (config/get :slack-socket/bot-token)))

(enrich/enrich
 (normalize/message-tree
  (archive/slurp-chan-day-raw arch "C010HTVBU0N" "2020-08-10")))
