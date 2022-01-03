FROM maven:3-jdk-11 AS build
COPY settings-docker.xml /usr/share/maven/ref/
COPY pom.xml /usr/src/app/pom.xml
COPY psc-api /usr/src/app/psc-api
COPY pscload /usr/src/app/pscload
RUN mvn -f /usr/src/app/pom.xml -gs /usr/share/maven/ref/settings-docker.xml clean package
FROM openjdk:11-slim-buster
COPY --from=build /usr/src/app/pscload/target/pscload-*.jar /usr/app/pscload.jar
USER daemon
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -jar /usr/app/pscload.jar"]
