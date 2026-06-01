(ns clojurians-log.config
  (:refer-clojure :exclude [get])
  (:require
   [lambdaisland.config :as config]))

(def prefix "clojurians-log")

(def config
  (config/create
   {:env :dev :prefix prefix}))

(def get (partial config/get config))
(def source (partial config/source config))
(def sources (partial config/sources config))
(def entries (partial config/entries config))
(def reload! (partial config/reload! config))
