(ns tutuv9.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.core.async :as async :refer [thread]]
            [clojure.tools.logging :as log]))

(def alarm (atom #{}))

(defn read-alarm [alarm-name]
  {:status (if (some #{alarm-name} @alarm) 204 200)
   :headers {"Content-Type" "text/plain"}
   :body (str (if (some #{alarm-name} @alarm) 204 200))})

(defn get-button [name]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "<html><head><title>Luxeria tutu</title></head><body><form method='POST' src='/" name "'><input type=\"submit\" value=\"RING!\" style=\"width:100%; height: 300px;background-color: #f44336;\"></form></body></html>")})

(add-watch alarm :watcher
           (fn [_ _ _ new-state]
             (when new-state
               (log/info "Alarm request sent at " (str new-state)))))

(defn set-alarm [alarm-name]
  (swap! alarm conj alarm-name)
  (async/thread
    (Thread/sleep 5000)
    (swap! alarm disj alarm-name)))

(defroutes app
  (GET "/" []
    (get-button ""))
  (GET "/check" []
    (read-alarm "default"))
  (POST "/" [] (set-alarm "default") (get-button ""))

  (GET "/:name" [name]
    (get-button name))
  (GET "/check/:name" [name]
    (read-alarm name))
  (POST "/:name" [name]
    (set-alarm name) (get-button name))

  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
