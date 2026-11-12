# Multi-arch
The application is built into a Docker image for `linux/amd64` and `linux/arm64` with [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) and Docker Buildx.

* [src/main/resources/docker/Dockerfile](src/main/resources/docker/Dockerfile) extracts the Spring Boot jar into layers (`dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`), so a code change only rebuilds the application layer.
* `docker:build` runs in the `package` phase. It builds both platforms and loads the native one into the local image store, since that store can't hold a multi-arch image.
* The tests run against the application on the JVM, not against the image. [multi-arch-image-test](../multi-arch-image-test) also tests the image.

The platforms are set by the `docker.platforms` property. To push the multi-arch image to a registry, give it a registry name and run `./mvnw package docker:push -Dapp.image=<registry>/<name>:<tag>`.


## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
