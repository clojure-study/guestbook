(ns guestbook.routes.login
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]])
  (:import (java.util Date)))

(defn login-page [{:keys [session flash]}]
  (layout/render "login.html"
        (merge {:session session}
           (select-keys flash [:name :password :errors]))
                 ))

(defn login! [{:keys [params]}]
  (if-let [user (first (db/signin-user params))]
     (-> (redirect "/")
      (assoc-in [:session :user-id] (:user_id user))
      (assoc-in [:session :user-name] (:name user))
    )
     (-> (redirect "/login")
         (assoc :flash (assoc params :errors {:password "Invalid name or password."})))
  ))

(defn logout! [{:keys [session]}]
  (-> (redirect "/")
      (assoc :session (dissoc session :user-id :user-name))))

(defroutes login-routes
           (GET "/login" request (login-page request))
           (POST "/login" request (login! request))
           (POST "/logout" request (logout! request))
           )
