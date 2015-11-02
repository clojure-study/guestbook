(ns guestbook.github
  (:require [compojure.core :refer :all]
            [clj-http.client :as http]
            [cemerick.url :refer [url-encode]]
            [slingshot.slingshot :refer [try+]]
            [ring.util.response :refer [redirect]]
            [clojure.data.json :as json]
            [guestbook.db :as db]
            [guestbook.signup.core :refer [duplicated-name?]]
            [environ.core :refer [env]]
            )
  (:import (java.util Date)))

;; reference
;; http://leonid.shevtsov.me/en/oauth2-is-easy


(def oauth2-params
  (merge {:authorize-uri  "https://github.com/login/oauth/authorize"
          :access-token-uri "https://github.com/login/oauth/access_token"}
         (env :github)))

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
                      :basic-auth [(:client-id oauth2-params) (:client-secret oauth2-params)]
                      :accept :json
                      })
          :body
          json/read-str)
      (catch Exception e (println e)))
    nil))


(defn get-user-info
  [access-token]
  (-> (http/get (str "https://api.github.com/user?access_token=" access-token))
      :body
      json/read-str))


;; TODO...
(defn- refresh-tokens
  "Request a new token pair"
  [refresh-token]
  (try+
   (let [{{access-token :access_token refresh-token :refresh_token} :body}
         (http/post (:access-token-uri oauth2-params)
                    {:form-params {:grant_type       "refresh_token"
                                   :refresh_token    refresh-token}
                     :basic-auth [(:client-id oauth2-params) (:client-secret oauth2-params)]
                     :as          :json})]
     [access-token refresh-token])
   (catch [:status 401] _ nil)))


;; TODO...
(defn get-fresh-tokens
  "Returns current token pair if they have not expired, or a refreshed token pair otherwise"
  [access-token refresh-token]
  (try+
   (and (get-user-info access-token)
        [access-token refresh-token])
   (catch [:status 401] _ (refresh-tokens refresh-token))))


(defn sign-up [name github-id]
  (db/save-user<! {:name name
                   :logintype "github"
                   :loginid github-id
                   :password nil
                   :timestamp (Date.)}))


(defn sign-in [user-info]
  (let [name (get user-info "name")
        github-id (str (get user-info "id"))
        user-id (-> {:logintype "github" :loginid github-id}
                    db/get-user-by-loginid
                    first
                    :user_id)]

    (if (nil? user-id)

      ;; auto sign-up
      (let [user (sign-up name github-id)]
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


(defroutes oauth2-github-routes
  ;; TODO test-csrf
  (GET "/oauth/github/login" request (redirect (authorize-uri oauth2-params "test-csrf")))
  (ANY "/oauth/github/callback" request
       (-> (get-authentication-response "test-csrf" (:params request))
           (get "access_token")
           get-user-info
           sign-in
           )))
