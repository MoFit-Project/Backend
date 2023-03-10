[![CI/CD](https://github.com/MoFit-Project/Backend/actions/workflows/gradle.yml/badge.svg)](https://github.com/MoFit-Project/Backend/actions/workflows/gradle.yml)

# Backend ì¤íë°©ë²

#### Project Tech Stack ð
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

#### Trouble Shooting ð¥ï¸ 
https://velog.io/@eenaa?tag=trouble-shooting

---
### ë°°í¬ ë°©ë²

#### 1. Openvidu ë°°í¬

- ì¤íë¹ëë¥¼ ì¤ì¹íê¸° ìí´ ê¶ì¥ê²½ë¡ `opt` ë¡ ì´ë
> cd /opt

- ì¤í ë¹ë ì¤ì¹

```
curl https://s3-eu-west-1.amazonaws.com/aws.openvidu.io/install_openvidu_latest.sh | bash
```
- ì¤í ë¹ë ì¤ì¹ ê²½ë¡ `/opt/openvidu` ìì íê²½ì¤ì 
```
$ vi .env

# OpenVidu configuration
# ----------------------
# ëë©ì¸ ëë í¼ë¸ë¦­IP ì£¼ì
DOMAIN_OR_PUBLIC_IP=mofit.kraftonjungle.shop

# ì¤íë¹ë ìë²ì íµì ì ìí ìí¬ë¦¿
OPENVIDU_SECRET=MY_SECRET

# Certificate type
CERTIFICATE_TYPE=letsencrypt

# ì´ë©ì¼ ì¤ì 
LETSENCRYPT_EMAIL=user@example.com

# HTTP port
HTTP_PORT=8442

# HTTPS port(í´ë¹ í¬í¸ë¥¼ íµí´ ì¤íë¹ë ìë²ì ì°ê²°)
HTTPS_PORT=8443
```

- ì¤ì  í `./openvidu start` ë¡ ì¤í ì´í(`ctrl + c`ë¥¼ ëë¥´ë©´ ë°±ê·¸ë¼ì´ëë¡ ì¤í)

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

#### 2. API ìë² ë°°í¬ ê³¼ì 

- GithubActionì íµí´ ëì»¤ ì´ë¯¸ì§ ìì± ë° docker-composeë¥¼ ì¤ííë¤. 
```
# ëì»¤ ì´ë¯¸ì§ ìì±ì ìí ë² ì´ì¤ ì´ë¯¸ì§
FROM adoptopenjdk:11-jdk-hotspot-focal

# ì íë¦¬ì¼ì´ì ì¤íì ìí íê²½ ë³ì ì¤ì 
ENV APP_HOME /app
ENV APP_VERSION 0.0.1-SNAPSHOT

# ì íë¦¬ì¼ì´ì JAR íì¼ì Docker ì´ë¯¸ì§ì ì¶ê°
#ADD target/mofit-backend-$APP_VERSION.jar $APP_HOME/app.jar
ADD build/libs/com-$APP_VERSION.jar $APP_HOME/app.jar
# Docker ì´ë¯¸ì§ìì ì¤íë  ëªë ¹ì´
CMD ["java", "-jar", "/app/app.jar"]
```

- Github Action íì¼
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
      if: contains(github.ref, 'main') # branchê° mainì¼ ë
      run: | #springì resources ê²½ë¡ë¡ ì´ë
        cd ./src/main/resources  
        touch ./application.yml # application.yml íì¼ ìì± 
        
        # GitHub-Actionsìì ì¤ì í ê°ì application.yml íì¼ì ì°ê¸°
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
          utcOffset: "+09:00" # ê¸°ì¤ì´ UTCì´ê¸° ëë¬¸ì íêµ­ìê°ì¸ KSTë¥¼ ë§ì¶ê¸° ìí´ +9ìê° ì¶ê°

      - name: Print Current Time
        run: echo "Current Time=${{steps.current-time.outputs.formattedTime}}" # current-time ìì ì§ì í í¬ë§·ëë¡ íì¬ ìê° ì¶ë ¥
        shell: bash
    
```

- ë³´ìì ìí´ ì¤íë§ì application.yml íì¼ì github secretì ì´ì©íì¬ ì ì¥

- ìì workflowì ìí´ ìë jobë¤ì íµí´ ë¹ë ë° ë°°í¬ë¥¼ ìííë¤.

- docker-compose.yml íì¼ì í´ë¹ íë¡ì í¸ìì ì¬ì©íë Redisëí ì»¨íì´ëë¡ ì¤ííë¤.

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
#### 3. Nginx ì¤ì ê³¼ ssl ì¸ì¦ì ë°ê¸ ë° ì ì©
WebRTCê¸°ë¥ì ì´ì©íë ¤ë©´ ì¹´ë©ë¼ë¥¼ ì¬ì©í´ì¼ íëë° ì´ë, ë°ëì https íµì ì´ ìêµ¬ëë¤. ë°ë¼ì SSL ì¸ì¦ìë¥¼ ë°ê¸ë°ìì¼ íëë° ì´ë, ëë©ì¸ì´ ìêµ¬ëë¤.

- Nginx ë¤ì´ë¡ë
```
# ì¤ì¹
sudo apt-get install nginx

# ì¤ì¹ íì¸ ë° ë²ì  íì¸
nginx -v
```
- letsencrypt ì¤ì¹
```
sudo apt-get install letsencrypt

sudo systemctl stop nginx

sudo letsencrypt certonly --standalone -d wwwì ì¸í ëë©ì¸ ì´ë¦
```

- Congratulations ë¡ ììíë ë¬¸êµ¬ê° ë³´ì´ë©´ ì¸ì¦ì ë°ê¸ì´ ìë£ëì¼ë©°
- ```/etc/nginx/live/```ëë©ì¸ì´ë¦ ì¼ë¡ ë¤ì´ê°ë©´ keyíì¼ë¤ì´ ìì ê²ì´ë¤.
- ì´í ```/etc/nginx/sites-available```ë¡ ì´ëí í, ì ë¹íì´ë¦```.conf``` íì¼ì ë§ë¤ì´ ì¤ë¤.

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
- ê·¸ í ì°¨ë¡ë¡ ëªë ¹ì ì¤ííë¤.
```
sudo ln -s /etc/nginx/sites-available/[íì¼ëª] /etc/nginx/sites-enabled/[íì¼ëª]

# ë¤ì ëªë ¹ì´ìì successfulì´ ë¨ë©´ nginxë¥¼ ì¤íí  ì ìë¤.
sudo nginx -t

sudo systemctl restart nginx
```
