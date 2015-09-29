(defproject guestbook "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.7.0-RC2"]
                 [selmer "0.8.2"]
                 [com.taoensso/timbre "3.4.0"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.66"]
                 [environ "1.0.1"]
                 [compojure "1.3.4"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-session-timeout "0.1.0"]
                 [metosin/ring-middleware-format "0.6.0"]
                 [metosin/ring-http-response "0.6.2"]
                 [bouncer "0.3.3"]
                 [prone "0.8.2"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [ring-server "0.4.0"]
                 [ragtime "0.3.9"]
                 [instaparse "1.4.0"]
                 [yesql "0.5.0-rc2"]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]

                 [clj-recaptcha "0.0.2"]

                 [clj-http "2.0.0"]
                 [mavericklou/clj-facebook-graph "0.5.3"]
                 ]

  :min-lein-version "2.0.0"
  :uberjar-name "guestbook.jar"
  :jvm-opts ["-server"]

;;enable to start the nREPL server when the application launches
;:env {:repl-port 7001}

  :main guestbook.core

  :plugins [[lein-ring "0.9.1"]
            [lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [ragtime/ragtime.lein "0.3.8"]]



  :ring {:handler guestbook.handler/app
         :init    guestbook.handler/init
         :destroy guestbook.handler/destroy
         :uberwar-name "guestbook.war"}



  :profiles
  {:uberjar {:omit-source true
             :env {:production true
                   :db-spec {:classname   "org.postgresql.Driver"
                             :subprotocol "postgresql"
                             :subname     "//127.0.0.1:5432/cks"
                             :user        "cks"
                             :password    "zmfhfwjzhfldktmxjel"
                             :make-pool?  true
                             :naming      {:keys   clojure.string/lower-case
                                           :fields clojure.string/upper-case}
                             }}

             :aot :all}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]
                        [pjstadig/humane-test-output "0.7.0"]
                        ]



         :repl-options {:init-ns guestbook.core}
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true
               :db-spec {:classname   "org.postgresql.Driver"
                         :subprotocol "postgresql"
                         :subname     "//127.0.0.1:5432/cks"
                         :user        "cks"
                         :password    "zmfhfwjzhfldktmxjel"
                         :make-pool?  true
                         :naming      {:keys   clojure.string/lower-case
                                       :fields clojure.string/upper-case}
                         }
               }
         }})
