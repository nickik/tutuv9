(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.core.async :as async :refer [thread]]
            [clojure.tools.logging :as log])
  (:import (org.joda.time DateTime)))

(def alarm (atom nil))

(defn read-alarm []
  {:status (if (nil? @alarm) 204 200)
   :headers {"Content-Type" "text/plain"}
   :body (str (if (nil? @alarm) 204 200))
    })

(defn get-button []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "<html><head><title>Luxeria tutu</title></head><body><form method='POST' src='/'><input type=\"submit\" value=\"RING!\" style=\"width:100%; height: 300px;background-color: #f44336;\"></form></body></html>")
   })

(add-watch alarm :watcher
           (fn [_ _ _ new-state]
             (when new-state
               (log/info "Alarm request sent at " (str new-state)))))

(defn set-alarm []
  (let [dt (DateTime.)]
    (reset! alarm dt)
    (async/thread
      (Thread/sleep 5000)
      (compare-and-set! alarm dt nil))))

(defroutes app
  (GET "/" []
    (get-button))
  (GET "/check" []
    (read-alarm))
  (POST "/" [] (set-alarm) (get-button))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
