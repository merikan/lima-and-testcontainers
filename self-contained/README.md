# Self-contained
The MariaDB container is defined entirely in test code. [AbstractIntegrationTest](src/test/java/com/merikan/testcontainers/todo/test/AbstractIntegrationTest.java) sets the server options with `withCommand` instead of a configuration file.

## Docker image
The [Dockerfile](Dockerfile) builds the application from source in three stages, so only Docker is needed on the host.

* The `maven` stage builds the jar with `./mvnw package`. Unit tests run, but integration tests don't, since they need Docker. The Maven repository is kept in a BuildKit cache mount, so dependencies are only downloaded once.
* The `builder` stage extracts the Spring Boot jar into layers (`dependencies`, `spring-boot-loader`, `snapshot-dependencies`, `application`).
* The runtime stage copies each layer separately, so a code change only rebuilds the application layer.

```
$ docker build -t todo-self-contained .
```

The `maven` and `builder` stages use `FROM --platform=$BUILDPLATFORM`, since the jar and its layers are the same on every platform. Only the runtime stage uses the target platform, and it runs nothing, so you can build for another platform without emulation:

```
$ docker build --platform linux/amd64 -t todo-self-contained .
```

The application needs a MariaDB database. Pass the connection settings as environment variables:

```
$ docker run -p 8080:8080 \
    -e APP_DB_HOST=<host> -e APP_DB_DATABASE=<database> \
    -e APP_DB_USER=<user> -e APP_DB_PASSWORD=<password> \
    todo-self-contained
```


## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
