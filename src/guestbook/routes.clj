(ns guestbook.routes
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :refer [ok]]
            [guestbook.db :as db]
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

(defn save-message! [{:keys [session params]}]
  (if-let [user-id (:user-id session)]

    (if-let [errors (validate-message params)]
      (-> (redirect "/")
          (assoc :flash (assoc params :errors errors)))
      (do
        (db/save-message! (assoc params :timestamp (Date.)))
        (redirect "/")))
    (redirect "/login")
    ))

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

(defn home-page [{:keys [session flash]}]
  (layout/render
    "home.html"
    (merge {:messages (db/get-messages)
            :session session}
           (select-keys flash [:name :message :errors]))))

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

(defn signup-page [{:keys [flash]}]
      (layout/render "signup.html"
                     (select-keys flash [:name :password :errors])
                     ))

(defn validate-user [params]
  (first
    (b/validate
      params
      :name [v/required [v/min-count 3]]
      :password [v/required [v/min-count 4]])))

(defn save-user! [{:keys [params]}]
  (if-let [errors (validate-user params)]
    (-> (redirect "/signup")
        (assoc :flash (assoc params :errors errors)))
    (let [updated-row-cnt (db/save-user! (assoc params :timestamp (Date.)))]
      (if (< 0 updated-row-cnt)
        (redirect "/login")
        (-> (redirect "/signup")
            (assoc :flash (assoc params :errors {:message "Duplicated name"})))))))


(defn about-page []
  (layout/render "about.html"))

(defn admin-page []
  (layout/render "admin.html"
                 {:users (db/get-names)}))

(defroutes app-routes
           (GET "/" request (home-page request))
           (POST "/" request (save-message! request))
           (POST "/delete/:id" [id] (delete-message! id))
           (GET "/update/:id" [id req] (update-message id req))
           (POST "/update" request (update-message! request))

           (GET "/login" request (login-page request))
           (POST "/login" request (login! request))
           (POST "/logout" request (logout! request))

           (GET "/signup" request (signup-page request))
           (POST "/signup" request (save-user! request))

           (GET "/admin" [] (admin-page))

           (GET "/about" [] (about-page))
           )
