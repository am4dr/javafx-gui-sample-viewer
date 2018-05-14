package com.github.am4dr.javafx.sample_viewer.internal;

import java.util.function.Function;

@FunctionalInterface
public interface UncheckedFunction<T, R> extends Function<T, R> {

    R applyEx(T t) throws Throwable;

    @Override
    default R apply(T t) {
        try {
            return applyEx(t);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    static <T, R> Function<T, R> uncheckedFunction(UncheckedFunction<T, R> function) {
        return function;
    }
    static <T, R> Function<T, R> uncheckedFunction(UncheckedFunction<T, R> function, R defaultValue) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable throwable) {
                return defaultValue;
            }
        };
    }
}
