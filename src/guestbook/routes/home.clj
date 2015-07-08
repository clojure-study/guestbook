(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]])
  (:import (java.util Date)))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (redirect "/")
        (assoc :flash (assoc params :errors errors)))
    (do
      (db/save-message! (assoc params :timestamp (Date.)))
      (redirect "/"))))

(defn delete-message! [id]
  (do
    (db/delete-message! {:id id})
    (redirect "/")))

(defn update-message [id {:keys [flash]}]
  (if (nil? id)
    (redirect "/")
    (layout/render
       "update.html"
       (merge (first (db/get-message {:id id}))
           (select-keys flash [:name :message :errors])))))

(defn update-message! [{:keys [params]}]
  (db/update-message! params)
  (redirect "/"))

(defn home-page [{:keys [flash]}]
  (layout/render
    "home.html"
    (merge {:messages (db/get-messages)}
           (select-keys flash [:name :message :errors]))))

(defn login-page []
  (layout/render "login.html"))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
           (GET "/" request (home-page request))
           (POST "/" request (save-message! request))
           (POST "/delete/:id" [id] (delete-message! id))
           (GET "/update/:id" [id req] (update-message id req))
           (POST "/update" request (update-message! request))
           (GET "/login" [] (login-page))
           (GET "/about" [] (about-page)))
