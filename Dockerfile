FROM maven:3-jdk-11 AS build
COPY settings-docker.xml /usr/share/maven/ref/
COPY pom.xml /usr/src/app
COPY psc-api /usr/src/app/
COPY pscload /usr/src/app/
RUN mvn -f /usr/src/app/pom.xml -gs /usr/share/maven/ref/settings-docker.xml clean package
FROM openjdk:11-slim-buster
COPY --from=build /usr/src/app/pscload/target/pscload-*.jar /usr/app/pscload.jar
USER daemon
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/app/pscload.jar"]
