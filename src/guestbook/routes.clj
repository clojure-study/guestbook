(ns guestbook.routes
  (:require [guestbook.layout :as layout]
            [guestbook.facebook :refer [facebook-callback]]
            [compojure.core :refer [defroutes GET POST DELETE PUT]]
            [ring.util.response :refer [content-type response]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [redirect]]
            [guestbook.signup.core :as signup ]
            )
  (:import (java.util Date)))

(defn validate-message [params]
  (first
    (b/validate
      params
      :name v/required
      :message [v/required [v/min-count 10]])))

(defn save-message! [{:keys [session params]}]
  (if-let [user-id (:user-id session)]

    (if-let [errors (validate-message params)]
      (-> (redirect "/guestbooks")
          (assoc :flash (assoc params :errors errors)))
      (do
        (db/save-message! (assoc params :timestamp (Date.)))
        (redirect "/guestbooks")))
    (redirect "/login")
    ))

(defn delete-message! [id]
  (do
    (db/delete-message! {:id id})
    (redirect "/guestbooks")))

(defn update-message [id {:keys [flash]}]
  (if (nil? id)
    (redirect "/guestbooks")
    (layout/render
       "update.html"
       (merge (first (db/get-message {:id id}))
           (select-keys flash [:name :message :errors])))))

(defn update-message! [{:keys [params]}]
  (db/update-message! params)
  (redirect "/guestbooks"))

(defn guest-page [{:keys [flash]}]
  (layout/render
    "guestbooks.html"
    (merge {:messages (db/get-messages)}
           (select-keys flash [:name :message :errors]))))

(defn login-page []
  (layout/render "login.html"
                 (select-keys flash [:name :password :errors])
                 ))

(defn login! [{:keys [params]}]
  (if-let [user (first (db/signin-user params))]
    (-> (redirect "/guestbooks")
        (assoc-in [:session :user-id] (:user_id user))
        (assoc-in [:session :user-name] (:name user))
        )
    (-> (redirect "/login")
        (assoc :flash (assoc params :errors {:password "Invalid name or password."})))
    ))

(defn logout! [{:keys [session]}]
  (-> (redirect "/guestbooks")
      (assoc :session (dissoc session :user-id :user-name))))


(defn about-page []
  (layout/render "about.html"))

(defn admin-page []
  (layout/render "admin.html" {:users (db/get-names)} ))

(defroutes app-routes
           (GET "/" request (guest-page request))
           (GET "/guestbooks" request (guest-page request))
           (POST "/guestbooks" request (save-message! request))
           (DELETE "/guestbooks/:id" [id] (delete-message! id))
           (GET "/guestbooks/:id/edit" [id req] (update-message id req))
           (PUT "/guestbooks" request (update-message! request))

           (GET "/login" [] (login-page))
           (POST "/login" request (login! request))
           (POST "/logout" request (logout! request))

           (GET "/login/facebook/callback" request (facebook-callback request))

           (GET "/signup" request (signup/go-page request))
           (POST "/signup" request (signup/signup! request))

           (GET "/admin" [] (admin-page))

           (GET "/about" [] (about-page))
           )
