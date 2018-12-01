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
