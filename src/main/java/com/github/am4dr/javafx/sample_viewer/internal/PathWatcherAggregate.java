package com.github.am4dr.javafx.sample_viewer.internal;

import com.github.am4dr.javafx.sample_viewer.PathWatchEvent;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;

public class PathWatcherAggregate {

    private final ExecutorService executorService = DaemonThreadFactory.getCachedThreadPool();
    private final Map<FileSystem, PathWatcher> fsToWatcher = new HashMap<>();
    private final List<Future<?>> watcherTasks = Collections.synchronizedList(new ArrayList<>());
    private final SubmissionPublisher<List<PathWatchEvent>> publisherDelegate = new SubmissionPublisher<>(executorService, 10);

    public Optional<PathWatcher> getOrCreate(Path path) {
        final var fileSystem = path.getFileSystem();
        return Optional.ofNullable(fsToWatcher.computeIfAbsent(fileSystem, fs -> {
            try {
                final var watcher = new PathWatcher(fs.newWatchService());
                final Future<?> task = executorService.submit(() -> watcher.watchBlocking(publisherDelegate::submit));
                watcherTasks.add(task);
                return watcher;
            } catch (IOException e) {
                return null;
            }
        }));
    }

    public void close() {
        watcherTasks.forEach(it -> it.cancel(true));
        publisherDelegate.close();
    }

    public Flow.Publisher<List<PathWatchEvent>> getPublisher() {
        return publisherDelegate;
    }
}
