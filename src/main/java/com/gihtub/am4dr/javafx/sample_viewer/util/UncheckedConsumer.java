package com.gihtub.am4dr.javafx.sample_viewer.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface UncheckedConsumer<T> extends Consumer<T> {

    void acceptEx(T t) throws Throwable;

    @Override
    default void accept(T t) {
        try {
            acceptEx(t);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    static <T> Consumer<T> uncheckedConsumer(UncheckedConsumer<T> callable) {
        return callable;
    }
}
