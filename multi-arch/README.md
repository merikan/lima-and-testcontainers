# Multi-arch
The application is built into a Docker image for `linux/amd64` and `linux/arm64` with [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) and Docker Buildx.

* [src/main/resources/docker/Dockerfile](src/main/resources/docker/Dockerfile) extracts the Spring Boot jar into layers (`dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`), so a code change only rebuilds the application layer.
* `docker:build` runs in the `pre-integration-test` phase. It builds both platforms and loads the native one into the local image store, since that store can't hold a multi-arch image.
* [ApplicationImageIT](src/test/java/com/merikan/testcontainers/todo/ApplicationImageIT.java) starts the image together with MariaDB on a shared network and calls the REST API.

The platforms are set by the `docker.platforms` property. To push the multi-arch image to a registry, give it a registry name and run `./mvnw package docker:push -Dapp.image=<registry>/<name>:<tag>`.


## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
