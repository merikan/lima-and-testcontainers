# Extracting the layers is architecture independent, so this stage always runs on the build machine's
# platform. No emulation is needed when building for another architecture.
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jre AS builder
WORKDIR /builder
# docker-maven-plugin adds the Spring Boot jar to the build context under maven/
COPY maven/*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# One image per target platform. Layers are copied least to most frequently changed,
# so a code change only rebuilds the application layer.
FROM eclipse-temurin:25-jre
WORKDIR /application
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]
