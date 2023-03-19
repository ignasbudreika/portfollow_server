FROM openjdk:17-jdk-alpine
ADD target/portfollow-0.0.1-SNAPSHOT.jar portfollow-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "portfollow-0.0.1-SNAPSHOT.jar"]