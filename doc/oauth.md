github oauth2

github 는 oauth2 에서 [authorization code grant type](https://tools.ietf.org/html/rfc6749#section-4.1) 을 지원한다.

## 간단 설명

 **클라이언트** (앱 서버)
 **유저-Agent** (ex 사용자 웹브라우저)
 **인증서버** (ex 깃허브-인증서버)

### 0. github 에서 미리 클라이언트를 등록한다.

- `client_id` (발급받음)
- `client_secret` (발급받음)
- `redirect_url` (등록함, ex) http://localhost:3000/oauth/callback)

### 1. 클라이언트가 유저-Agent 를 인증서버로 리다이렉트 시킨다.

```
ex) GET https://github.com/login/oauth/authorize?response_type=code&client_id=...&redirect_url=...&scope=...&state=...
```

- `client_id`
- `redirect_url` (인증 후 콜백 url, 앱 등록시 redirect_url 과 같아야 한다.)
- `scope` (유저 데이터 접근 범위)
- `state` (리다이렉트로 콜백시 유저 확인용도)

### 2. 인증서버에서 유저 인증후, authorization code 데이터를 가지고 클라이언트
의 redirect_url 주소로 돌아온다.

### 3. 클라이언트는 유저를 통해 받은 authorization code 정보로 access_token 을 받는다.

```
ex) POST https://github.com/login/oauth/access_token
```

- `code`
- `client_id`
- `client_secret`
- `redirect_url`
- `state`

### 4. access-token 을 가지고 유저 데이터를 조회한다.

```
ex) GET https://api.github.com/user?access_token=...
```
