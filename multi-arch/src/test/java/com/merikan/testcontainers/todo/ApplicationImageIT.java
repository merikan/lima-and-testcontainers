package com.merikan.testcontainers.todo;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests the application image built by docker-maven-plugin, running against MariaDB on a shared network.
 */
@Testcontainers
class ApplicationImageIT {
    private static final DockerImageName MARIADB_IMAGE = DockerImageName.parse("mariadb:12.3.3");
    private static final DockerImageName APP_IMAGE = DockerImageName.parse(System.getProperty("app.image"));
    private static final String MARIADB_ALIAS = "mariadb";
    private static final int APP_PORT = 8080;

    private static final Network network = Network.newNetwork();

    @Container
    private static final MariaDBContainer mariadb = new MariaDBContainer(MARIADB_IMAGE)
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci", "--max-connections=50")
        .withNetwork(network)
        .withNetworkAliases(MARIADB_ALIAS);

    @Container
    private static final GenericContainer<?> app = new GenericContainer<>(APP_IMAGE)
        .withNetwork(network)
        .withEnv("APP_DB_HOST", MARIADB_ALIAS)
        .withEnv("APP_DB_PORT", "3306")
        .withEnv("APP_DB_DATABASE", mariadb.getDatabaseName())
        .withEnv("APP_DB_USER", mariadb.getUsername())
        .withEnv("APP_DB_PASSWORD", mariadb.getPassword())
        .withExposedPorts(APP_PORT)
        .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200))
        .dependsOn(mariadb);

    private RequestSpecification spec;

    @BeforeEach
    void setUp() {
        spec = new RequestSpecBuilder()
            .setBaseUri("http://" + app.getHost())
            .setPort(app.getMappedPort(APP_PORT))
            .setBasePath("/todos")
            .build();
    }

    @Test
    void shouldCreateAndGetTodo() {
        long id = given(spec)
            .contentType(ContentType.JSON)
            .body("{\"title\": \"Build a multi-arch image\", \"finished\": false}")
            .when().post()
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");

        given(spec)
            .when().get("/{id}", id)
            .then().statusCode(200)
            .body("title", equalTo("Build a multi-arch image"));
    }
}
