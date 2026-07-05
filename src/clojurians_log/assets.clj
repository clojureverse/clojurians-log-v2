(ns clojurians-log.assets
  "Serve assets directly from memory, handle HEAD and ETag"
  (:require
   [clojure.java.io :as io])
  (:import
   [java.security MessageDigest]))

(set! *warn-on-reflection* true)

(defn md5-etag [^bytes data]
  (let [digest (MessageDigest/getInstance "MD5")
        hash   (.digest digest data)]
    (str
     "\""
     (apply str (map #(format "%02x" (bit-and % 0xff)) hash))
     "\"")))

(def css
  ["assets/fonts/inter.css"
   (if (io/resource "public/css/compiled/ornament.css")
     "assets/css/compiled/ornament.css"
     "styles.css")])

(def js
  ["assets/js/main.js"])

(defn make-route [path content-type]
  (let [content (.getBytes (slurp path) "UTF-8")
        etag (md5-etag content)
        size (alength content)
        get-res {:status 200
                 :body content
                 :headers {"Content-Type" content-type
                           "Content-Length" (str size)
                           "Cache-Control" "max-age=600"
                           "ETag" etag}}
        head-res (assoc get-res :body "")
        not-modified {:status 304 :body ""}]
    [(str "/" path)
     {:get (fn [{:keys [headers] :as req}]
             (prn "GET" path)
             (if (or (= etag (get headers "if-none-match"))
                     (= "*" (get headers "if-none-match")))
               not-modified
               get-res))
      :head (constantly head-res)}]))

(defn routes []
  (vec
   (for [[content-type files] {"text/css" css "application/javascript" js}
         file files
         :when (.exists (io/file file))]
     (make-route file content-type))))
