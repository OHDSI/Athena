package com.odysseusinc.athena.glue;

import com.odysseusinc.athena.util.Fn;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A value container class to allow different step definitions to shared data between steps without depending on each other.
 * The name is chosen inspired by recommendations from picocontainer documentation. Since we can use spring to instantiate it,
 * we don't need to use the picocontainer itself (and probably can't, as it will conflict with cucumber-spring), however it is
 * still nice to have it named very differently from "context", which are already way too many around.
 */
@Component
@Slf4j
public class World {

    private static final Pattern IN_QUOTES = Pattern.compile("^\"(.+)\"$");
    @Getter
    private transient Object cursor;

    private transient Map<String, String> refs = new HashMap<>();

    /**
     * Shorthand method to set cursor with failover support and capture the result under given reference name
     * @param name name to capture under
     * @param callable Supplier to provide the value
     * @param <T> result type
     */
    <T> void setCursor(String name, Function<T, ?> id, Callable<T> callable) {
        setCursor(() -> {
            T result = callable.call();
            capture(name, String.valueOf(id.apply(result)));
            return result;
        });
    }
    /**
     * Support for failover methods. Since idiomatic cucumber tests have verification as a separate step, positive or negative,
     * glue methods that support negative outcome in tests should fail silently while recording the failure into the {@link #cursor}
     * field for future assertion.
     * @param callable Supplier to provide the value
     */
    <T> void setCursor(Callable<T> callable) {
        try {
            Fn.castAs(cursor, Throwable.class).ifPresent(throwable -> {
                log.error(throwable.getMessage(), throwable);
                Assertions.fail("The previous step failed with error that was not consumed by assertion: " + throwable.getMessage());
            });

            cursor = callable.call();
            if (cursor instanceof CompletableFuture<?>) {
                CompletableFuture<?> future = (CompletableFuture<?>) cursor;
                cursor = future.join();
            }
        } catch (Exception e) {
            // This could be expected error, so log only one line.
            log.info(e.getClass() + ":" + e.getMessage());
            cursor = e;
            log.info("Captured thrown exception {}: {}", e.getClass(), e.getMessage());
        }
    }


    public String ref(String name) {
        return refs.get(name);
    }

    public void capture(String name, UUID uuid) {
        capture(name, String.valueOf(uuid));
    }

    public void capture(String name, String value) {
        if (name != null) {
            Optional<String> optionalRef = Optional.ofNullable(ref(name));
            if (optionalRef.isPresent()) {
                Assertions.assertEquals(optionalRef.get(), value);
            } else {
                refs.put(name, value);
            }
        }
    }
    public String resolveAlias(String value) {
        if (value == null) {
            return null;
        } else {
            Matcher matcher = IN_QUOTES.matcher(value);
            if (matcher.find()) {
                return ref(matcher.group(1));
            } else {
                return value;
            }
        }
    }

    public void setCursor(Object cursor) {
        setCursor(() -> cursor);
    }

    public void resetCursor() {
        cursor = null;
    }

}
