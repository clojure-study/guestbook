===
# sandbox
===

### tryclojure (https://github.com/Raynes/tryclojure)

이 기능은 tryclojure 를 보고 따라해보았음.

tryclojure 는 sandbox 기능을 clojail 라이브러리에서 가져다 썼음.


### clojail (https://github.com/Raynes/clojail)

간단히 말해서 JVM's built in sandboxing 을 사용하기 때문에 ~/.java.policy 파일 필요.

.java.policy 파일 예제

```
grant {
  permission java.security.AllPermission;
};
```

사용방법은 clojail Github 에 소개되어있음.


### clojail.testers

현재 버전에서는 serializable-fn 에서 가져온 시리얼 함수를 사용하기 때문에
clojail 에서 나오는 불안전한 코드등 '무엇이든지 간에' 값을 받아넘길 수 있게 되었다고 함.

핵심은 serializable 하다는 것이고, java interop 를 안전하게 하기 위해 필수이다.
(아마 샌드박스에 시리얼한 상태로 넘기고, 시리얼을 풀때 가능한 문제를 샌드박스 내부로 한정할 수 있는다는 뜻인듯)


### 경고

이 라이브러리는 블랙리스트 기반으로 작성된 대상만을 체크하기 때문에 언제든 구멍이 생길 수 있다.
그나마 다행인 것은 JVM sandbox 기반이기 때문에 클로저 샌드박스가 고장나도 I/O 까지 고장나진 않으며,
JVM sandbox 를 망가뜨려야 시스템 접근이 가능한데 이것은 매우 어렵다.