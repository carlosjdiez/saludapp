FROM eclipse-temurin:21-jdk-alpine
VOLUME /tmp
EXPOSE 8081
COPY target/saludapp-0.1.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]