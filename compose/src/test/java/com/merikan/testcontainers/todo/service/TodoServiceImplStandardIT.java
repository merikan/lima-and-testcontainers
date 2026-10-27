package com.merikan.testcontainers.todo.service;

import com.merikan.testcontainers.todo.model.Todo;
import com.merikan.testcontainers.todo.repository.TodoRepository;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.merikan.testcontainers.todo.test.TestRandom.newEasyRandom;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("integrationtest")
@Testcontainers
@Disabled("This test is disabled since we are using abstract base class instead. See this as an example of how-to")
public class TodoServiceImplStandardIT {
    private static final DockerImageName MARIADB_IMAGE = DockerImageName.parse("mariadb:12.3.3");
    private final EasyRandom random = newEasyRandom();

    @Container
    private static final MariaDBContainer mariadb = new MariaDBContainer(MARIADB_IMAGE);

    @Autowired
    private TodoService uut;

    @Autowired
    private TodoRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        repository.deleteAll();
    }

    @Nested
    class Save {
        @Test
        public void shouldSave() {
            Todo todo = random.nextObject(Todo.class);
            Todo persisted = uut.save(todo);
            assertThat(persisted).isEqualToIgnoringGivenFields(todo, "id");
            assertThat(repository.findById(persisted.getId())).isNotEmpty();

        }

        @Test
        public void shouldThrowExceptionWhenTitleIsEmpty() {
            Todo todo = random.nextObject(Todo.class);
            todo.setTitle("");
            ConstraintViolationException e = assertThrows(ConstraintViolationException.class, () -> {
                uut.save(todo);
            });
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
            assertThat(violations).isNotEmpty();
            assertThat(violations).extracting(ConstraintViolation::getMessage, v -> v.getPropertyPath().toString())
                .contains(tuple("must not be blank", "title"));

        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
    }

}
