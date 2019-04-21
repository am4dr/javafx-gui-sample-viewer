package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.DaemonThreadFactory;
import com.github.am4dr.javafx.sample_viewer.internal.PathWatcherImpl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public final class PathWatchEventPublisher implements Flow.Publisher<List<PathWatchEvent>> {

    private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
    private final SubmissionPublisher<List<PathWatchEvent>> publisherDelegate = new SubmissionPublisher<>(DEFAULT_EXECUTOR_SERVICE, 10);
    private final List<Future<?>> watcherTasks = Collections.synchronizedList(new ArrayList<>());
    private final WatcherStore watchers = new WatcherStore();


    @Override
    public void subscribe(Flow.Subscriber<? super List<PathWatchEvent>> subscriber) {
        publisherDelegate.subscribe(subscriber);
    }

    public synchronized void shutdown() {
        watcherTasks.forEach(it -> it.cancel(true));
        publisherDelegate.close();
    }

    public void addRecursively(Path path) {
        final var absolutePath = path.normalize().toAbsolutePath();
        final var watcher = watchers.getOrCreate(absolutePath);
        watcher.ifPresent(it -> it.addRecursively(path));
    }


    private class WatcherStore {

        private final Map<FileSystem, PathWatcherImpl> fsToWatcher = new HashMap<>();

        public Optional<PathWatcherImpl> getOrCreate(Path path) {
            final var fileSystem = path.getFileSystem();
            return Optional.ofNullable(fsToWatcher.computeIfAbsent(fileSystem, fs -> {
                try {
                    final var watcher = new PathWatcherImpl(fs.newWatchService());
                    watcherTasks.add(DEFAULT_EXECUTOR_SERVICE.submit(() -> watcher.watchBlocking(publisherDelegate::submit)));
                    return watcher;
                } catch (IOException e) {
                    return null;
                }
            }));
        }
    }
}
