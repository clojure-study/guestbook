(ns guestbook.core
  (:require [guestbook.handler :refer [app init destroy]]
            [ring.adapter.jetty :refer [run-jetty]]

            [ring.middleware.reload :as reload]
            [ragtime.main]
            [taoensso.timbre :as timbre]
            [environ.core :refer [env]])
  (:gen-class))

(defn parse-port [[port]]
  (Integer/parseInt (or port (env :port) "3000")))





(defonce server (atom nil))

(defn start-server [port]
  (init)
  (reset! server
          (run-jetty
            (if (env :dev) (reload/wrap-reload #'app) app)
            {:port port
             :join? false})))

(defn stop-server []
  (when @server
    (destroy)
    (.stop @server)
    (reset! server nil)))

(defn start-app [args]
  (let [port (parse-port args)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
    (start-server port)))


(defn migrate [args]
  (ragtime.main/-main
    "-r" "ragtime.sql.database"
    "-d" (let [db-spec (env :db-spec)]
           (str "jdbc:" (:subprotocol db-spec) ":" (:subname db-spec) "?user=" (:user db-spec) "&password=" (:password db-spec)))
    "-m" "ragtime.sql.files/migrations"
    (clojure.string/join args)))

(defn -main [& args]
  (case (first args)
    "migrate" (migrate args)
    "rollback" (migrate args)
    (start-app args)))
