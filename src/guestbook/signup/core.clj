(ns guestbook.signup.core
  (:require [guestbook.layout :as layout]
            [guestbook.db :as db]
            [clj-recaptcha.client-v2 :as recaptcha]
            [ring.util.response :refer [redirect]]
            [bouncer.core :as b]
            [bouncer.validators :as v])
  (:import (java.util Date)))


(defn recaptcha-valid? [g-recaptcha-response]
  (let [valid (recaptcha/verify "6LcUqAwTAAAAAMUjkfdBGPUuQVJ0OZy4tBcVq-8J" g-recaptcha-response)]
    (:valid valid)))

(defn go-page [{:keys [flash]}]
  (layout/render "signup.html"
                 (select-keys flash [:name :password :errors])
                 ))

(defn validate-user [params]
  (first
    (b/validate
      params
      :name [v/required [v/min-count 3]]
      :password [v/required [v/min-count 4]])))

(defn signup! [{:keys [params]}]
  (if (not (recaptcha-valid? (:g-recaptcha-response params)))
    (redirect "/signup")
    (if-let [errors (validate-user params)]
      (-> (redirect "/signup")
          (assoc :flash (assoc params :errors errors)))
      (let [updated-row-cnt (db/save-user! (assoc params :timestamp (Date.)))]
        (if (< 0 updated-row-cnt)
          (redirect "/login")
          (-> (redirect "/signup")
              (assoc :flash (assoc params :errors {:message "Duplicated name"}))))))))