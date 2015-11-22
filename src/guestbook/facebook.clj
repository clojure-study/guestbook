(ns guestbook.facebook
  (:require [compojure.core :refer :all]
            [clj-http.client :as http]
            [cemerick.url :refer [url-encode]]
            [slingshot.slingshot :refer [try+]]
            [ring.util.response :refer [redirect]]
            [clojure.data.json :as json]
            [guestbook.db :as db]
            [guestbook.signup.core :refer [duplicated-name?]]
            [environ.core :refer [env]])
  (:import (java.util Date)))

(defonce oauth2-params
  (merge {:authorize-uri  "https://www.facebook.com/dialog/oauth"
          :access-token-uri "https://graph.facebook.com/oauth/access_token"}
         (env :facebook)))

(defn authorize-uri [client-params csrf-token]
  (str
   (:authorize-uri client-params)
   "?response_type=code"
   "&client_id="
   (url-encode (:client-id client-params))
   "&redirect_uri="
   (url-encode (:redirect-uri client-params))
   "&scope="
   (url-encode (:scope client-params))
   "&state="
   (url-encode csrf-token)))

(defn get-authentication-response [csrf-token response-params]
  (if (= csrf-token (:state response-params))
    (try
      (-> (http/post (:access-token-uri oauth2-params)
                     {:form-params {:code         (:code response-params)
                                    :grant_type   "authorization_code"
                                    :client_id    (:client-id oauth2-params)
                                    :redirect_uri (:redirect-uri oauth2-params)}
                      :basic-auth [(:client-id oauth2-params)
                                   (:client-secret oauth2-params)]
                      })
          :body
          str)
      (catch Exception e (println e)))
    nil))

(defn sign-up [name facebook-id]
  (db/save-user<! {:name name
                   :logintype "facebook"
                   :loginid facebook-id
                   :password nil
                   :timestamp (Date.)}))

(defn sign-in [user-info]
  (let [name (get user-info "name")
        facebook-id (str (get user-info "id"))
        user-id (-> {:logintype "facebook" :loginid facebook-id}
                    db/get-user-by-loginid
                    first
                    :user_id)]

    (if (nil? user-id)

      ;; auto sign-up
      (let [user (sign-up name facebook-id)]
        (if (nil? user) ;; sign-up fail
          (-> (redirect "/login"))
          (-> (redirect "/guestbooks")
              (assoc-in [:session :user-id] (:user_id user))
              (assoc-in [:session :logintype] (:logintype user))
              (assoc-in [:session :user-name] (:name user)))))

      (let [user (first (db/get-user {:userid user-id}))]
        (-> (redirect "/guestbooks")
            (assoc-in [:session :user-id] (:user_id user))
            (assoc-in [:session :logintype] (:logintype user))
            (assoc-in [:session :user-name] (:name user))))
      )))


(defn get-user-info
  [access-token]
  (-> (http/get (str "https://graph.facebook.com/me?" access-token))
      :body
      json/read-str))

(defroutes oauth2-facebook-routes
  ;; TODO test-csrf
  (GET "/oauth/facebook/login" request
       (redirect (authorize-uri oauth2-params "test-csrf")))
  (ANY "/oauth/facebook/callback" request
       (-> (get-authentication-response "test-csrf" (:params request))
           get-user-info
           sign-in)))
