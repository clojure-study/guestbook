(ns guestbook.guestbook.core
  (:require [guestbook.layout :as layout]
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
