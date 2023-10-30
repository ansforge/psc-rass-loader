FROM maven:3-jdk-11 AS build
COPY settings-docker.xml /usr/share/maven/ref/
COPY pom.xml /usr/src/app/pom.xml
COPY pscload /usr/src/app/pscload
ARG PROSANTECONNECT_PACKAGE_GITHUB_TOKEN
RUN mvn -f /usr/src/app/pom.xml -gs /usr/share/maven/ref/settings-docker.xml -Dinternal.repo.username=${PROSANTECONNECT_PACKAGE_GITHUB_TOKEN} clean package
FROM openjdk:11-slim-buster
COPY --from=build /usr/src/app/pscload/target/pscload-*.jar /usr/app/pscload.jar
RUN mkdir -p /app/files-repo
RUN chown -R daemon: /app/files-repo
USER daemon
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java -jar /usr/app/pscload.jar"]
