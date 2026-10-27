# Lima and Testcontainers

## Intro

This is a Spring Boot application used as an example in my blog post about using Lima instead of Docker Desktop

## The source code

Three ways to configure containers with Testcontainers, named by where the configuration lives.

- [self-contained](self-contained): the container is defined entirely in test code.
- [project-mounted](project-mounted): the container is defined in test code, with its configuration in [src/test/resources/mariadb-conf/my.cnf](project-mounted/src/test/resources/mariadb-conf/my.cnf).
- [compose](compose): the container is defined in [src/test/resources/compose.yml](compose/src/test/resources/compose.yml) and started through `ComposeContainer`.

## How to run the code

You need Java 25 and a running Docker environment, such as Lima.

1. Check out the code

```
$ git clone https://github.com/merikan/lima-and-testcontainers.git
$ cd lima-and-testcontainers
```

2. Run the unit and integration tests for all projects

```
$ ./mvnw verify
```

or for one project: `self-contained`, `project-mounted` or `compose`

```
$ cd self-contained
$ ./mvnw verify
```

## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
