## Description
gRPC Swagger Web Portal

## Compile from docker
- Configure grpc-swagger port in Dockerfile:

We must set the **server.port** property in Dockerfile if we want 
change default port 8080 to run grpc-swagger service. 

On example could be:


```
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/grpc-swagger.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar --server.port=8888"]
```

- Compile Docker image:

```
docker build -t grpc-swagger-web .
```

## Start from docker

The emvironment variables to configured are:

- SWAGGER_UI_TITLE: gRPC Server Title.
- GRPC_HOST: gRPC Server Host.
- GRPC_PORT:  gRPC Server Port.

If be change the default port of the grpc-swagger service export the same port

On example could be:

```
docker run -d -it --name grpc-swagger-web /
-e SWAGGER_UI_TITLE='gRPC Proxy Services' -e GRPC_HOST='localhost' -e GRPC_PORT=9090 /
-p 8888:8888 grpc-swagger-web
```