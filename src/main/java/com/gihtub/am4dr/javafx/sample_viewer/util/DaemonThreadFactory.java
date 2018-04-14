package com.gihtub.am4dr.javafx.sample_viewer.util;

import java.util.concurrent.ThreadFactory;

public enum DaemonThreadFactory implements ThreadFactory {
    INSTANCE;

    @Override
    public Thread newThread(Runnable r) {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        return thread;
    }
}
