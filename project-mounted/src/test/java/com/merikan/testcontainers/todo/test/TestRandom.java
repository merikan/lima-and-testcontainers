package com.merikan.testcontainers.todo.test;

import com.merikan.testcontainers.todo.model.Todo;
import org.instancio.Instancio;
import org.instancio.Model;

import java.util.stream.Stream;

import static org.instancio.Select.field;

/**
 * Random {@code Todo}s with {@code id} left null, so saved todos are inserted
 * and the database generates the id.
 */
public final class TestRandom {
    private static final Model<Todo> TODO = Instancio.of(Todo.class)
        .ignore(field(Todo::getId))
        .toModel();

    private TestRandom() {
    }

    public static Todo newTodo() {
        return Instancio.create(TODO);
    }

    public static Stream<Todo> newTodos(int count) {
        return Instancio.stream(TODO).limit(count);
    }
}
