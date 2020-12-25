## uni-reslover icon driver
Universal resolver 에 ICON driver 를 추가하여 ICON 에서 사용하는 DID 를 조회 하는 프로젝트  


## 실행방법  
### 1. uni-reslover-web docker image build  
현재 `uni-resolver-web` 에는 icon driver 가 적용되어 있지 않아 icon driver 를 사용하기 위해서 로컬에서 docker image 를 build 한다.  
```sh
docker build -f ./resolver/java/uni-resolver-web/docker/Dockerfile . -t universalresolver/uni-resolver-web
```
  
  
### 2. docker-compose 실행  
`docker-compose-universal.yml` 설정 파일을 지정해 docker 를 실행한다.  
```sh
docker-compose -f docker-compose-universal.yml up
```
  
### 3. request    
shell 에서 다음 명령을 실행하면 icon did 에 대한 document 를 확인할 수 있다.
```sh
curl -X GET http://localhost:8080/1.0/identifiers/did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8
```  

### 4. result  
정상적으로 실행되었으면 다음과 같이 리턴한다.  
```json
{"didDocument":{
  "@context" : "https://www.w3.org/2019/did/v1",
  "id" : "did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8",
  "service" : [ ],
  "authentication" : [ {
    "publicKey" : [ "sampleKey1" ]
  } ],
  "publicKey" : [ {
    "id" : "sampleKey1",
    "type" : "Secp256k1VerificationKey",
    "created" : 10815604,
    "publicKeyBase64" : "BDJUC6E2WLJsJ9d1Q7Sptnxwnrh0ygZcDPa4yp+Sj970MioKNIY2YAzT2kBghcbuQS5A6xPKzcE/BAM1bBqJ1Z4=",
    "revoked" : 0
  } ]
},"content":null,"contentType":null,"resolverMetadata":{"duration":3211,"identifier":"did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8","driverId":"driver-woosiiik/driver-did-icon","didUrl":{"didUrlString":"did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8","did":{"didString":"did:icon:02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8","method":"icon","methodSpecificId":"02:1b7aef9ba939d5730e2c2a52fe35578fe8d7a67ac137c4c8","parseTree":null,"parseRuleCount":null},"parameters":null,"parametersMap":{},"path":"","query":null,"fragment":null,"parseTree":null,"parseRuleCount":null}},"methodMetadata":{}}
```
  

## Driver 개발  
[confluence/Universal Resolver Drivcer 개발](https://icon-project.atlassian.net/wiki/spaces/DID/pages/206209213/Universal+Resolver+Driver) 를 참고   
  
  
## universal-resolver version
release 하기 전, `0.3.0` version 이고 github 에서 `88f6935f` commit 을 checkout 받음  
  

## driver 배포 방법  
다음 script 를 실행하면 `driver-did-icon` docker image 와 `icon-resolver-driver.zip` 파일이 생성된다.
```sh
.archive.sh
```
위 파일들과 [icon drvier 설치 가이드](/docs/icon-driver-install-guide.md) 를 전달한다.  
  
  
### 참고
[github/universal-resolver](https://github.com/decentralized-identity/universal-resolver)  
[confluence](https://icon-project.atlassian.net/wiki/spaces/DID/pages/192151564/Universal+Resolver)  

