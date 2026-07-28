(ns clojurians-log.assets
  "Serve assets directly from memory, handle HEAD and ETag"
  (:require
   [clojure.java.io :as io])
  (:import
   [java.nio.file Files]
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
   (if (io/resource "assets/ornament.css")
     "assets/ornament.css"
     "styles.css")])

(def js
  ["assets/js/main.js"])

(def fonts
  ["assets/fonts/inter-v18-latin_latin-ext-regular.woff2"
   "assets/fonts/inter-v18-latin_latin-ext-italic.woff2"
   "assets/fonts/inter-v18-latin_latin-ext-500.woff2"
   "assets/fonts/inter-v18-latin_latin-ext-600.woff2"
   "assets/fonts/inter-v18-latin_latin-ext-700.woff2"
   "assets/fonts/inter-v18-latin_latin-ext-800.woff2"
   "assets/fonts/inter-v18-latin_latin-ext-900.woff2"])

(def imgs
  ["assets/imgs/trash.png"])

(defn make-route [path content-type]
  (let [^java.io.File f (io/file path)
        ^bytes content (Files/readAllBytes (.toPath f))
        etag (md5-etag content)
        size (alength content)
        get-res {:status 200
                 :body content
                 :headers {"Content-Type" content-type
                           "Content-Length" (str size)
                           "Cache-Control" (str "max-age=" (* 60 60 24))
                           "ETag" etag}}
        head-res (assoc get-res :body "")
        not-modified {:status 304 :body ""}]
    [(str "/" path)
     {:get (fn [{:keys [headers] :as _req}]
             (if (or (= etag (get headers "if-none-match"))
                     (= "*" (get headers "if-none-match")))
               not-modified
               get-res))
      :head (constantly head-res)}]))

(defn routes []
  (vec
   (for [[content-type files] {"text/css"               css
                               "application/javascript" js
                               "font/woff2"             fonts
                               "image/png"              imgs}
         file files
         :when (.exists (io/file file))]
     (make-route file content-type))))
