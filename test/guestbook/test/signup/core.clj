(ns guestbook.test.signup.core
  (:require [clojure.test :refer :all]
            [guestbook.signup.core :refer :all]))

(deftest redirect-errors-test
  (testing "captcha is false"
    (is (= {:status 302
            :headers {"Location" "/signup"}
            :body ""
            :flash {:errors {:message "captcha"}}}
           (redirect-errors {:has-captcha-error true :duplicated-name :any :has-field-errors :any} :any)) ))
  (testing "has error"
    (is (= {:status 302
            :headers {"Location" "/signup"}
            :body ""
            :flash {:errors {}}}
           (redirect-errors {:has-captcha-error false :duplicated-name :any :has-field-errors {}} {})) ))
  (testing "sss"
    (is (= {:status 302
            :headers {"Location" "/signup"}
            :body ""
            :flash {:errors {:message "중복된 계정입니다."}}}
           (redirect-errors {:has-captcha-error false :duplicated-name :any :has-field-errors nil} {})))))


(deftest signup-redirect-test
  (testing "redirect-errors"
    (with-redefs-fn {#'guestbook.signup.core/validate-signup (fn [_] {:has-captcha-error true})
                     #'guestbook.signup.core/redirect-errors (fn [_ _] :redirect-errors)}
      #(is (= :redirect-errors (signup! :any)))))

  (testing "redirect-login"
    (with-redefs-fn {#'guestbook.signup.core/validate-signup (fn [_] nil)
                     #'guestbook.signup.core/save-user! (fn [_] :save-user )}
      #(is (= :save-user (signup! :any))))))

(deftest save-user!-test
  (with-redefs-fn {#'guestbook.db/save-user! (fn [_] {:status "OK"})}
    #(is (= {:status 302
             :headers {"Location" "/login"},
             :body ""}
            (save-user! {:name "abc" :password "def"})))))

(deftest validate-signup-test
  (with-redefs-fn
    {#'guestbook.signup.core/recaptcha-valid? (fn [_] true)
     #'guestbook.signup.core/validate-user (fn [_] {})
     #'guestbook.signup.core/duplicated-name? (fn [_] true)}
    #(is (= {:has-captcha-error false
             :has-field-errors {}
             :duplicated-name true}
            (validate-signup :any))))
  (with-redefs-fn
    {#'guestbook.signup.core/recaptcha-valid? (fn [_] true)
     #'guestbook.signup.core/validate-user (fn [_] nil)
     #'guestbook.signup.core/duplicated-name? (fn [_] false)}
    #(is (nil? (validate-signup :any)))))





