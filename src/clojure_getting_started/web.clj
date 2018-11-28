(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]])
  (:import (org.joda.time DateTime)
           (org.joda.time.format ISODateTimeFormat)))

(def alarm (atom (DateTime.)))

(defn read-alarm []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body
    (str (. (ISODateTimeFormat/dateTime) print @alarm))})

(defn set-alarm []
  (reset! alarm (DateTime.)))

(defroutes app
  (GET "/" []
    (read-alarm))
  (POST "/" [] (set-alarm) (read-alarm))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
