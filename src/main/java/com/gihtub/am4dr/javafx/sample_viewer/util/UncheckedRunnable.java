package com.gihtub.am4dr.javafx.sample_viewer.util;

@FunctionalInterface
public interface UncheckedRunnable extends Runnable {

    void runEx() throws Throwable;

    @Override
    default void run() {
        try {
            runEx();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    static Runnable uncheckedRunnable(UncheckedRunnable runnable) {
        return runnable;
    }
}
