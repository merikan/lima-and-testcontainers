package com.merikan.testcontainers.todo.test;

import com.merikan.testcontainers.todo.model.Todo;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import static org.jeasy.random.FieldPredicates.inClass;
import static org.jeasy.random.FieldPredicates.named;

/**
 * {@code EasyRandom} that leaves {@code Todo.id} null, so saved todos are inserted
 * and the database generates the id.
 */
public final class TestRandom {
    private TestRandom() {
    }

    public static EasyRandom newEasyRandom() {
        return new EasyRandom(new EasyRandomParameters()
            .excludeField(named("id").and(inClass(Todo.class))));
    }
}
