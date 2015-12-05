# nil.camp
[![Build Status](https://travis-ci.org/clojure-study/guestbook.svg?branch=master)](https://travis-ci.org/clojure-study/guestbook)
## 접속주소
http://52.68.124.223

## 개발 시작하기

### 1. 프로젝트 받기
    $ git clone https://github.com/clojure-study/guestbook.git
    $ cd guestbook

### 2. PostgreSQL 설치 및 실행
[http://www.postgresql.org/download/](http://www.postgresql.org/download/) 에서 OS에 따라 설치하고 실행한다.

##### 맥 사용자의 경우
맥의 경우 가장 쉬운 방법은 다음과 같다: [http://postgresapp.com/](http://postgresapp.com/) 에서 다운로드하고, `/Applications`로 옮기고, 더블클릭하여 실행한다.

##### 우분투 사용자의 경우 쉬운 설정방법
설치 이후 간편 사용 설정법은 [링크](https://github.com/soulawaker/PostgreSQL-for-UBUNTU/blob/master/Ubutu-postgreSQL.md#다른-서버-설정-방법쉬운-방법)의 내용을 참조한다.

### 3. psql 접속 및 User, Database  생성 
psql 에 접속하여  user 와 Database 를 생성한다. 

#### Database 생성 

`project.clj` 의 :profiles :dev :db-spec 참고하여 database 를 생성한다.  

    psql# CREATE DATABASE {database name};

#### User 생성 

    psql# CREATE USER {user name} WITH PASSWORD {password name};

### 4. DB 초기 셋업
    $ lein run migrate

### 5. 프로젝트 실행
    $ lein run

### 6. 확인
웹 브라우저로 [http://localhost:3000](http://localhost:3000) 에 접속하여 확인

## 배포하기
#### 빌드
    $ cd guestbook
    $ lein uberjar
    
#### 파일 업로드
    $ scp -i "clojurestudy-aws.pem" .lein-env ec2-user@52.68.124.223:~
    $ scp -i "clojurestudy-aws.pem" target/guestbook.jar ec2-user@52.68.124.223:~/target
pem 파일의 path는 배포자에 따라 다를 수 있음. 또한, .lein-env에서 :production이 true임을 주의하자.

#### 서버 재시작
ssh로 접속하여 screen 세션에 들어감
    
    $ ssh -i "clojurestudy-aws.pem" ec2-user@52.68.124.223
    $ screen -r

서버 정지하고 80포트로 다시 시작

    Ctrl + c
    $ sudo java -jar target/guestbook.jar 80

세션에서 나옴    
    
    Ctrl + a + d
