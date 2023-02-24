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
