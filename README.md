[![CI/CD](https://github.com/MoFit-Project/Backend/actions/workflows/gradle.yml/badge.svg)](https://github.com/MoFit-Project/Backend/actions/workflows/gradle.yml)

# Backend 실행방법

#### Project Tech Stack 📚
&#160;   
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat&logo=Spring Boot&logoColor=white"/> 
<img src="https://img.shields.io/badge/NGINX-009638?style=flat&logo=NGINX&logoColor=white"/>
<img src="https://img.shields.io/badge/Amazon AWS-232F3E?style=flat&logo=Amazon AWS&logoColor=white"/> 
<img src="https://img.shields.io/badge/Amazon RDS-527FFF?style=flat&logo=Amazon RDS&logoColor=white"/> 
&#160;   
<img src="https://img.shields.io/badge/Docker-2496ED?style=flat&logo=Docker&logoColor=white"/>
<img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=flat&logo=GitHub Actions&logoColor=white"/>
<img src="https://img.shields.io/badge/WebRTC-333333?style=flat&logo=WebRTC&logoColor=white"/> 
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=MySQL&logoColor=white"/>
<img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=Redis&logoColor=white"/>

---
### 배포 방법

#### 1. Openvidu 배포

- 오픈비두를 설치하기 위해 권장경로 `opt` 로 이동
> cd /opt

- 오픈 비두 설치

```
curl https://s3-eu-west-1.amazonaws.com/aws.openvidu.io/install_openvidu_latest.sh | bash
```
- 오픈 비두 설치 경로 `/opt/openvidu` 에서 환경설정
```
$ vi .env

# OpenVidu configuration
# ----------------------
# 도메인 또는 퍼블릭IP 주소
DOMAIN_OR_PUBLIC_IP=mofit.kraftonjungle.shop

# 오픈비두 서버와 통신을 위한 시크릿
OPENVIDU_SECRET=MY_SECRET

# Certificate type
CERTIFICATE_TYPE=letsencrypt

# 이메일 설정
LETSENCRYPT_EMAIL=user@example.com

# HTTP port
HTTP_PORT=8442

# HTTPS port(해당 포트를 통해 오픈비두 서버와 연결)
HTTPS_PORT=8443
```

- 설정 후 `./openvidu start` 로 실행 이후(`ctrl + c`를 누르면 백그라운드로 실행)

```
$ ./openvidu start

Creating openvidu-docker-compose_coturn_1          ... done
Creating openvidu-docker-compose_app_1             ... done
Creating openvidu-docker-compose_kms_1             ... done
Creating openvidu-docker-compose_nginx_1           ... done
Creating openvidu-docker-compose_redis_1           ... done
Creating openvidu-docker-compose_openvidu-server_1 ... done

----------------------------------------------------

   OpenVidu Platform is ready!
   ---------------------------

   * OpenVidu Server: https://DOMAIN_OR_PUBLIC_IP/

   * OpenVidu Dashboard: https://DOMAIN_OR_PUBLIC_IP/dashboard/

----------------------------------------------------

```

---

#### 2. API 서버 배포 과정

- GithubAction을 통해 도커 이미지 생성 및 docker-compose를 실행한다. 
```
# 도커 이미지 생성을 위한 베이스 이미지
FROM adoptopenjdk:11-jdk-hotspot-focal

# 애플리케이션 실행을 위한 환경 변수 설정
ENV APP_HOME /app
ENV APP_VERSION 0.0.1-SNAPSHOT

# 애플리케이션 JAR 파일을 Docker 이미지에 추가
#ADD target/mofit-backend-$APP_VERSION.jar $APP_HOME/app.jar
ADD build/libs/com-$APP_VERSION.jar $APP_HOME/app.jar
# Docker 이미지에서 실행될 명령어
CMD ["java", "-jar", "/app/app.jar"]
```

- Github Action 파일
```
# This workflow uses actions that are not certified by GitHub.
# They are provided by a third-party and are governed by
# separate terms of service, privacy policy, and support
# documentation.
# This workflow will build a Java project with Gradle and cache/restore any dependencies to improve the workflow execution time
# For more information see: https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-gradle

name: CI/CD
 
on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

permissions:
  contents: read

jobs:
  CI-CD:

    runs-on: ubuntu-latest

    ##JDK setting
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
          
      
    ## create application.yml
    - name: make application.yml
      if: contains(github.ref, 'main') # branch가 main일 때
      run: | #spring의 resources 경로로 이동
        cd ./src/main/resources  
        touch ./application.yml # application.yml 파일 생성 
        
        # GitHub-Actions에서 설정한 값을 application.yml 파일에 쓰기
        echo "${{ secrets.YML }}" > ./application.yml 
      shell: bash
    
    ## gradle build
    - name: Build with Gradle
      uses: gradle/gradle-build-action@67421db6bd0bf253fb4bd25b31ebb98943c375e1
      with:
        arguments: build
    
    
    ## docker build & push to production
    - name: Docker build & push
      if: contains(github.ref, 'main')
      run: |
          docker login -u ${{ secrets.DOCKER_USERNAME }} -p ${{ secrets.DOCKER_PASSWORD }}
          docker build -f Dockerfile -t ${{ secrets.DOCKER_REPO }}/mofit-backend:latest .
          docker push ${{ secrets.DOCKER_REPO }}/mofit-backend
## deploy to develop
    - name: Deploy to dev
      uses: appleboy/ssh-action@master
      id: deploy-dev
      if: contains(github.ref, 'main')
      with:
        host: ${{ secrets.HOST }}
        username: ${{ secrets.USERNAME }}
        password: ${{ secrets.PASSWORD }}
        port: 22
        #key: ${{ secrets.PRIVATE_KEY }}
        script: |
            cd ../home/ubuntu
            sudo docker rm -f $(docker ps -qa)
            sudo docker pull ${{ secrets.DOCKER_REPO }}/mofit-backend
            docker-compose up -d
            docker image prune -f
  ## time
  current-time:
    needs: CI-CD
    runs-on: ubuntu-latest
    steps:
      - name: Get Current Time
        uses: 1466587594/get-current-time@v2
        id: current-time
        with:
          format: YYYY-MM-DDTHH:mm:ss
          utcOffset: "+09:00" # 기준이 UTC이기 때문에 한국시간인 KST를 맞추기 위해 +9시간 추가

      - name: Print Current Time
        run: echo "Current Time=${{steps.current-time.outputs.formattedTime}}" # current-time 에서 지정한 포맷대로 현재 시간 출력
        shell: bash
    
```

- 보안을 위해 스프링의 application.yml 파일은 github secret을 이용하여 저장

- 위의 workflow에 속해 있는 job들을 통해 빌드 및 배포를 수행한다.

- docker-compose.yml 파일에 해당 프로젝트에서 사용하는 Redis또한 컨테이너로 실행한다.

```
version: '3'

services:
  redis:
    image: redis
    restart: always
    networks:
      - mofit_network

  mofit_backend:
    image: eenaa/mofit-backend:latest
    restart: always
    environment:
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
    ports:
      - "8080:8080"
    depends_on:
      - redis
    networks:
      - mofit_network

networks:
  mofit_network:
    driver: bridge

```
#### 3. Nginx 설정과 ssl 인증서 발급 및 적용
WebRTC기능을 이용하려면 카메라를 사용해야 하는데 이때, 반드시 https 통신이 요구된다. 따라서 SSL 인증서를 발급받아야 하는데 이때, 도메인이 요구된다.

- Nginx 다운로드
```
# 설치
sudo apt-get install nginx

# 설치 확인 및 버전 확인
nginx -v
```
- letsencrypt 설치
```
sudo apt-get install letsencrypt

sudo systemctl stop nginx

sudo letsencrypt certonly --standalone -d www제외한 도메인 이름
```

- Congratulations 로 시작하는 문구가 보이면 인증서 발급이 완료됐으며
- ```/etc/nginx/live/```도메인이름 으로 들어가면 key파일들이 있을 것이다.
- 이후 ```/etc/nginx/sites-available```로 이동한 후, 적당한이름```.conf``` 파일을 만들어 준다.

```
server {

        location /{
                proxy_pass http://localhost:8080/;
        }

        location /api {
                proxy_pass http://localhost:8080/api;
        }

    listen 443 ssl; 
    ssl_certificate /etc/letsencrypt/live/mofit.bobfriend.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/mofit.bobfriend.site/privkey.pem;

}

server {
    if ($host = mofit.bobfriend.site) {
        return 301 https://$host$request_uri;
    } 

        listen 80;
        server_name mofit.bobfriend.site;
    return 404; 
}
```
- 그 후 차례로 명령을 실행한다.
```
sudo ln -s /etc/nginx/sites-available/[파일명] /etc/nginx/sites-enabled/[파일명]

# 다음 명령어에서 successful이 뜨면 nginx를 실행할 수 있다.
sudo nginx -t

sudo systemctl restart nginx
```
