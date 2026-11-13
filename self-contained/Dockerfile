# vim: ft=dockerfile

# Build the jar with Maven in a separate container
# The jar and its extracted layers are the same on every platform, so the build stages
# run on the build platform and a cross-platform build needs no emulation
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS maven
WORKDIR /build

COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
COPY src/ src/

# Cache the Maven repository between builds
# Integration tests need Docker, so only unit tests run here
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B package

# Perform the extraction in a separate builder container
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jre AS builder
WORKDIR /builder

# Copy the jar file from the maven container and rename it to application.jar
COPY --from=maven /build/target/*.jar application.jar

# Extract the jar file using an efficient layout
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# This is the runtime container
FROM eclipse-temurin:25-jre
WORKDIR /application

# Copy the extracted jar contents from the builder container into the working directory in the runtime container
# Every copy step creates a new docker layer
# This allows docker to only pull the changes it really needs
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
# Start the application jar - this is not the uber jar used by the builder
# This jar only contains application code and references to the extracted jar files
# This layout is efficient to start up and AOT cache (and CDS) friendly
ENTRYPOINT ["java", "-jar", "application.jar"]
