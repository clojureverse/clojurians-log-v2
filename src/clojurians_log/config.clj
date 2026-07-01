(ns clojurians-log.config
  (:refer-clojure :exclude [get])
  (:require
   [lambdaisland.config :as config]
   [lambdaisland.config.systemd-creds :as system-creds]
   [lambdaisland.config.cli :as cli]))

(def prefix "clojurians-log")

(def config
  (-> {:prefix prefix}
      config/create
      system-creds/add-provider
      cli/add-provider))

(def get (partial config/get config))
(def source (partial config/source config))
(def sources (partial config/sources config))
(def entries (partial config/entries config))
(def reload! (partial config/reload! config))
