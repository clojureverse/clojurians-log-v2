(ns clojurians-log.system
  (:require
   [clojurians-log.config :as config]
   [lambdaisland.makina.app :as app]))

(def system
  (app/create
   {:prefix config/prefix
    :data-readers {'config config/get}}))

(def load! (partial app/load! system))
(def start! (partial app/start! system))
(def stop! (partial app/stop! system))
(def value (partial app/value system))
(def state (partial app/state system))
(def component (partial app/component system))
(def refresh (partial app/refresh `system))
(def refresh-all (partial app/refresh-all `system))

(comment
  (load!)
  (start!)
  (refresh-all)
  @system
  (value)
  (state)
  (start! [:clojurians-log.db])
  (stop!  [:clojurians-log.db])
  )

;; =>
