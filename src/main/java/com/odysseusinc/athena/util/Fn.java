package com.odysseusinc.athena.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Groups together functional utility methods with a very wide scope of usage
 * that can't be classified into a specific business domain.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Fn {
    public static <T> T create(Supplier<T> constructor, Consumer<? super T> initializer) {
        T result = constructor.get();
        initializer.accept(result);
        return result;
    }

}
