(ns guestbook.sandbox
  (:require [clojail.testers :refer [secure-tester-without-def blanket]]
            [clojail.core :refer [sandbox]]
            [clojure.stacktrace :refer [root-cause]]
            [clojure.data.json :as json]
            [ring.util.response :refer [response]]
            [ring.util.response :refer [redirect]]
            [compojure.core :refer [defroutes GET POST DELETE PUT]]
            [guestbook.layout :as layout])
  (:import java.io.StringWriter
           java.util.concurrent.TimeoutException))

;; ref
;; https://github.com/Raynes/tryclojure/blob/master/src/tryclojure/models/eval.clj

;; 작성자의 변: 아직 실력이 부족하여, 코드 외에 주석, 설명을 많이 달아놓았습니다. -_ㅜ;
;; 아직 product 라기 보다는 study 프로젝트라고 제 맘대로 해석(?)해서 스터디용 주석을 넣었습니다.
;; 양해바랍니다. 제 코드 리팩토링은 언제든지 환영입니다. ㅜㅜ!! 해주세염.

(defn eval-form [form sb]
  ;; 사용자가 println 등으로 stdout 되는 값을 얻기위해 *out* 을 이용함
  (with-open [out (StringWriter.)]
    (let [result (sb form {#'*out* out})]
      {:expr form
       :result [out result]})))

;; *read-eval* ?
;; http://stackoverflow.com/questions/12337815/what-does-the-variable-read-eval-do
;; read-string 인자로 #= 로 시작하는 경우 eval 을 허용할지를 결정
;; example>
;; (binding [*read-eval* false] (read-string "#=(eval (System/exit 1))"))
;; (binding [*read-eval* true] (read-string "#=(eval (System/exit 1))"))

(defn eval-string [expr sb]
  ;; read-string 으로 #= 로 시작하는 코드 평가하지 않도록 *read-eval* 끄기
  (let [form (binding [*read-eval* false] (read-string expr))]
    (eval-form form sb)))

(def guestbook-tester
  ;; 샌드박스 블랙리스트 목록에 guestbook 으로 시작하는 네임스페이스도 추가
  (conj secure-tester-without-def (blanket "guestbook")))

(defn make-sandbox []
  (sandbox guestbook-tester
           :timeout 2000
           :init '(do (require '[clojure.repl :refer [doc source]])
                      (future (Thread/sleep 600000)
                              (-> *ns* .getName remove-ns)))))

(defn find-sb [session]
  (let [sb* (session :sb)]
    (if (or (nil? sb*) (nil? @sb*))
      (atom (make-sandbox))
      sb*)))

(defn eval-request [expr sb*]
  (try
    (eval-string expr @sb*)
    (catch TimeoutException _
      {:error true :message "Execution Timed Out!"})
    (catch Exception e
      {:error true :message (str (root-cause e))})))

(defn eval-json [expr sb*]
  (let [{:keys [expr result error message] :as res} (eval-request expr sb*)
        data (if error
               res
               (let [[out res] result]
                 {:expr (pr-str expr)
                  :result (str out (pr-str res))}))]
    (json/write-str data)))

(defn play-sandbox [req]
  (let [expr (get-in req [:params :expr])
        session (:session req)
        results (get session :results [])
        sb* (find-sb session)]
    (-> (redirect "/sandbox")
        (assoc :session (-> session
                            (assoc :sb sb*)
                            ;; TODO 리팩토링
                            ;; 지금은 eval 결과를 session 에 쌓아두고 보내주는 구조.
                            ;; 나중에 결과 기록 위치를 클라이언트쪽으로 옮겨야 함.
                            (assoc :results (conj results (eval-json expr sb*))))))))

(defroutes sandbox-routes
  (GET "/sandbox" req (layout/render "sandbox.html"))
  (POST "/sandbox" req (play-sandbox req)))
