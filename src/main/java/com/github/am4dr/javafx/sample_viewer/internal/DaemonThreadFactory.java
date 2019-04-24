package com.github.am4dr.javafx.sample_viewer.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public enum DaemonThreadFactory implements ThreadFactory {
    INSTANCE;

    private static ExecutorService CACHED_THREAD_POOL = Executors.newCachedThreadPool(INSTANCE);

    @Override
    public Thread newThread(Runnable r) {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }

    public static ExecutorService getCachedThreadPool() {
        return CACHED_THREAD_POOL;
    }
}
