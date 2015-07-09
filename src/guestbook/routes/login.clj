(ns guestbook.routes.login
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]])
  (:import (java.util Date)))

(defn login-page []
  (layout/render "login.html"))

(defn login [{:keys [params]}]
  (println params)
  (redirect "/"))

(defroutes login-routes
           (GET "/login" [] (login-page))
           (POST "/login" request (login request))
           )
