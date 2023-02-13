FROM openjdk:8-jdk-alpine
MAINTAINER masalinas.gancedo@gmail.com
COPY target/grpc-swagger.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar", "--server.port=8888"]