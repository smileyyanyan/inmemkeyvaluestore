FROM openjdk:17-jdk-slim
EXPOSE 9888
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]