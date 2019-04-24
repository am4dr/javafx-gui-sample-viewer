package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.PathWatcherAggregate;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Flow;

public final class PathWatchEventPublisher implements Flow.Publisher<List<PathWatchEvent>> {

    private final PathWatcherAggregate watchers = new PathWatcherAggregate();

    @Override
    public void subscribe(Flow.Subscriber<? super List<PathWatchEvent>> subscriber) {
        watchers.getPublisher().subscribe(subscriber);
    }

    public synchronized void shutdown() {
        watchers.close();
    }

    public void addRecursively(Path path) {
        final var absolutePath = path.normalize().toAbsolutePath();
        final var watcher = watchers.getOrCreate(absolutePath);
        watcher.ifPresent(it -> it.addRecursively(path));
    }
}
