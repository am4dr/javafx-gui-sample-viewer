package com.gihtub.am4dr.javafx.sample_viewer.util;

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
}
