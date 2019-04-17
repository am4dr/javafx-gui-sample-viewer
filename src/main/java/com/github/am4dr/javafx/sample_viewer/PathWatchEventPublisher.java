package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.DaemonThreadFactory;

import java.util.List;
import java.util.concurrent.*;

public final class PathWatchEventPublisher implements Flow.Publisher<List<PathWatcher.PathWatchEvent>> {

    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
    private final SubmissionPublisher<List<PathWatcher.PathWatchEvent>> publisherDelegate;
    private final Future<?> watcherTask;

    public PathWatchEventPublisher(PathWatcher watcher) {
        this(watcher, DEFAULT_EXECUTOR_SERVICE, DEFAULT_EXECUTOR_SERVICE);
    }

    public PathWatchEventPublisher(PathWatcher watcher,
                                   ExecutorService publisherExecutor,
                                   ExecutorService watcherExecutor) {
        publisherDelegate = new SubmissionPublisher<>(publisherExecutor, 10);
        watcherTask = watcherExecutor.submit(() -> watcher.watchBlocking(publisherDelegate::submit));
    }


    @Override
    public void subscribe(Flow.Subscriber<? super List<PathWatcher.PathWatchEvent>> subscriber) {
        publisherDelegate.subscribe(subscriber);
    }

    public synchronized void shutdown() {
        watcherTask.cancel(true);
        publisherDelegate.close();
    }
}
