(ns guestbook.routes.login
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]])
  (:import (java.util Date)))

(defn login-page [{:keys [session]}]
  (layout/render "login.html" {:session session}))

(defn login! [{:keys [params]}]
  (-> (redirect "/")
      (assoc-in [:session :user-id] (:id params))))

(defn logout! [{:keys [session]}]
  (-> (redirect "/")
      (assoc :session (dissoc session :user-id))))

(defroutes login-routes
           (GET "/login" request (login-page request))
           (POST "/login" request (login! request))
           (POST "/logout" request (logout! request))
           )
