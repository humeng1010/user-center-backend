FROM maven:3.5-jdk-8-alpine as builder

# copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Builder a release artifact.
RUN mvn package -DskipTests

# Run the web service on container startup.
CMD ["java","-jar","/app/target/user-center-backend-0.0.1.jar","--spring.profiles.active=pro"]
