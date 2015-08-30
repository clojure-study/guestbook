(ns guestbook.facebook
  (:require [clj-facebook-graph.auth :refer :all]
            [clj-facebook-graph.client :as client]
            [ring.util.response :refer [redirect]]
            [guestbook.db :as db])
  (:import (java.util Date)))

(defonce facebook-app-info
  {:client-id "1068136783211255"
   :client-secret "d36aa393d055fc311ff097ba2dc40719"
   :redirect-uri "http://52.68.124.223:3000/login/facebook/callback"
   :scope ["email"]})

(defn get-facebook-user [facebook-id]
  (first (db/get-facebook-user {:facebookid facebook-id})))

(defn goto-guestbooks [user]
  (-> (redirect "/guestbooks")
      (assoc-in [:session :user-id] (:user_id user))
      (assoc-in [:session :user-name] (:name user))))

(defn sign-up [facebook-id token]
  (with-facebook-auth {:access-token token}
    (let [me (client/get [:me])
          new-user {:name (:name (:body me))
                    :facebookid facebook-id
                    :password ""
                    :timestamp (Date.)}]
      (db/save-user! new-user)
      (get-facebook-user facebook-id))))

(defn facebook-callback [{:keys [params]}]
  (let [{:keys [facebook-id token]} params
        user (get-facebook-user facebook-id)]

    (if (not (empty? user))

      ;; already user
      (goto-guestbooks user)

      ;; new user => sign-up
      (let [user (sign-up facebook-id token)]
        (if (empty? user)

          ;; sign-up fail
          (-> (redirect "/login")
              (assoc :flash (assoc params :errors {:message "Facebook login fail!"})))

          ;; sign-up success
          (goto-guestbooks user))))))
