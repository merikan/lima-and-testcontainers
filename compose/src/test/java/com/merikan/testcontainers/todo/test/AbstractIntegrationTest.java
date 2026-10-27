package com.merikan.testcontainers.todo.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integrationtest")
public abstract class AbstractIntegrationTest {
    private static final DockerImageName DOCKER_IMAGE = DockerImageName.parse("docker:25.0.5");
    private static final String MARIADB_SERVICE = "mariadb-1";
    private static final int MARIADB_PORT = 3306;
    private static final ComposeContainer environment;

    static {
        environment = new ComposeContainer(DOCKER_IMAGE, new File("src/test/resources/compose.yml"))
            .withExposedService(MARIADB_SERVICE, MARIADB_PORT);
        environment.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("app.db.host", () -> environment.getServiceHost(MARIADB_SERVICE, MARIADB_PORT));
        registry.add("app.db.port", () -> environment.getServicePort(MARIADB_SERVICE, MARIADB_PORT));
        registry.add("app.db.database", () -> "test");
        registry.add("app.db.user", () -> "test");
        registry.add("app.db.password", () -> "test");
    }
}
