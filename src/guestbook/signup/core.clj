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
    (:valid? valid)))

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

(defn redirect-errors [errors params]
  (if (:has-captcha-error errors)
    (-> (redirect "/signup") (assoc :flash {:errors {:message "captcha"}}))
    (if (:has-field-errors errors)
      (-> (redirect "/signup") (assoc :flash (assoc params :errors (:has-field-errors errors))))
      (if (:duplicated-name errors)
        (-> (redirect "/signup") (assoc :flash (assoc params :errors {:message "중복된 계정입니다."})))))))


(defn duplicated-name? [params]
  (= 1 (count (db/check-user-exists params))))

(defn validate-signup [params]
  (let [errors {:has-captcha-error (not (recaptcha-valid? (:g-recaptcha-response params)))
                :has-field-errors (validate-user params)
                :duplicated-name (duplicated-name? params)}]
    (if (or (:has-captcha-error errors)
            (:has-field-errors errors)
            (:duplicated-name errors))
      errors)))

(defn save-user! [params]
  (do (db/save-user! (assoc params :facebookid "" :timestamp (Date.)))
      (redirect "/login")))

(defn signup!
  ([{:keys [params]}]
   (if-let [errors (validate-signup params)]
     (redirect-errors errors params)
     (save-user! params))))
