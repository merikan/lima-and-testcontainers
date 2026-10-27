# Project-mounted
The MariaDB container is defined in test code, but its configuration comes from files in the project directory.
`MariaDBContainer.withConfigurationOverride("mariadb-conf")` copies [src/test/resources/mariadb-conf](src/test/resources/mariadb-conf) to `/etc/mysql/conf.d` in the container.


## License

This project is licensed under the terms of the [Apache License, Version 2.0](LICENSE).
