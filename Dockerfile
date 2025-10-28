FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package
FROM tomcat:10.1-jdk21-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/chat-service.war /usr/local/tomcat/webapps/ROOT.war
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
CMD ["catalina.sh","run"]
