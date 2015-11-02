(ns guestbook.routes
  (:require [guestbook.layout :as layout]
            [guestbook.guestbook.core :as guestbook]
            [compojure.core :refer [defroutes GET POST DELETE PUT]]
            [ring.util.response :refer [content-type response]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db :as db]
            [ring.util.response :refer [redirect]]
            [guestbook.signup.core :as signup ]
            ))
(defn login-page []
  (layout/render "login.html"))

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
           (GET "/" request (guestbook/guest-page request))
           (GET "/guestbooks" request (guestbook/guest-page request))
           (POST "/guestbooks" request (guestbook/save-message! request))
           (DELETE "/guestbooks/:id" [id] (guestbook/delete-message! id))
           (GET "/guestbooks/:id/edit" [id req] (guestbook/update-message id req))
           (PUT "/guestbooks" request (guestbook/update-message! request))

           (GET "/login" [] (login-page))
           (POST "/login" request (login! request))
           (POST "/logout" request (logout! request))

           (GET "/signup" request (signup/go-page request))
           (POST "/signup" request (signup/signup! request))

           (GET "/admin" [] (admin-page))

           (GET "/about" [] (about-page))
           )
