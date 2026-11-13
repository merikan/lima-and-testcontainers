# Lima and Testcontainers

## Intro

This is a Spring Boot application used as an example in my blog post about using Lima instead of Docker Desktop

## The source code

Three ways to configure containers with Testcontainers, named by where the configuration lives.

- [self-contained](self-contained): the container is defined entirely in test code.
- [project-mounted](project-mounted): the container is defined in test code, with its configuration in [src/test/resources/mariadb-conf/my.cnf](project-mounted/src/test/resources/mariadb-conf/my.cnf).
- [compose](compose): the container is defined in [src/test/resources/compose.yml](compose/src/test/resources/compose.yml) and started through `ComposeContainer`.

The application itself can also run in a container:

- [self-contained](self-contained): the application is built from source into an image with a multi-stage [Dockerfile](self-contained/Dockerfile).
- [multi-arch](multi-arch): the application is built into a multi-architecture image (`linux/amd64`, `linux/arm64`) with docker-maven-plugin and Buildx. The tests run against the application on the JVM.
- [multi-arch-image-test](multi-arch-image-test): same as `multi-arch`, but the image is also tested as a container.
- [buildpacks](buildpacks): same as `multi-arch-image-test`, but the images for `linux/amd64` and `linux/arm64` are built with buildpacks through the Spring Boot Maven plugin. On Lima it needs emulation for the non-native platform and a symlink to the rootless Docker socket in the VM, see [buildpacks](buildpacks/README.md#lima-setup).

## How to run the code

You need Java 25 and a running Docker environment, such as Lima.

1. Check out the code

```
$ git clone https://github.com/merikan/lima-and-testcontainers.git
$ cd lima-and-testcontainers
```

2. Run the unit and integration tests for all projects except `multi-arch`, `multi-arch-image-test` and `buildpacks`

```
$ ./mvnw verify
```

Building the multi-arch images is slow, so `multi-arch`, `multi-arch-image-test` and `buildpacks` are only included with the `multi-arch` profile

```
$ ./mvnw verify -Pmulti-arch
```

or for one project: `self-contained`, `project-mounted`, `compose`, `multi-arch`, `multi-arch-image-test` or `buildpacks`

```
$ cd self-contained
$ ./mvnw verify
```

## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
