(ns nrepl-poke)

(keys @@#'nrepl.middleware.session/sessions)
;; => ("fc3ffb31-dacc-4af0-93fa-2a4c588782bb" "e406123c-8ba9-4d4c-b0d5-d2116b70ef6b")
(meta (get @@#'nrepl.middleware.session/sessions "fc3ffb31-dacc-4af0-93fa-2a4c588782bb"))
(get (deref (get @@#'nrepl.middleware.session/sessions "fc3ffb31-dacc-4af0-93fa-2a4c588782bb"))
     #'clojure.core/*print-namespace-maps*)

(doseq [sess (vals @@#'nrepl.middleware.session/sessions)]
  (swap! sess assoc #'clojure.core/*print-namespace-maps* false))
