package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.DaemonThreadFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public final class FileUpdatePublisher implements Flow.Publisher<Path> {

    private final ExecutorService executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
    private SubmissionPublisher<Path> submission = new SubmissionPublisher<>(executor, 10);

    @Override
    public void subscribe(Flow.Subscriber<? super Path> subscriber) {
        submission.subscribe(subscriber);
    }

    public synchronized void shutdown() {
        watchServices.values().forEach(it -> {
            try {
                it.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        submission.close();
        executor.shutdown();
    }

    private final Map<FileSystem, WatchService> watchServices = Collections.synchronizedMap(new HashMap<>());
    private final Map<WatchKey, Path> watchedDirs = Collections.synchronizedMap(new HashMap<>());

    public void addDirectory(Path path) {
        final Path absolutePath = path.toAbsolutePath();
        if (!Files.isDirectory(absolutePath)) {
            throw new IllegalArgumentException("path must be a directory");
        }
        if (watchedDirs.containsValue(absolutePath)) {
            return;
        }
        final var fileSystem = absolutePath.getFileSystem();
        final var watchService = watchServices.computeIfAbsent(fileSystem, fs -> {
            final WatchService ws = getWatchService(fs);
            takeUpdateAsync(ws);
            return ws;
        });
        try {
            final WatchKey key = absolutePath.register(watchService, ENTRY_MODIFY, ENTRY_CREATE);
            watchedDirs.put(key, absolutePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void takeUpdateAsync(WatchService watchService) {
        executor.submit(() -> {
            while (!Thread.interrupted()) {
                final WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    break;
                }
                key.pollEvents().stream()
                        .map(WatchEvent::context)
                        .map(Path.class::cast)
                        .forEach(submission::submit);
                key.reset();
            }
        });
    }

    private static WatchService getWatchService(FileSystem fileSystem) {
        try {
            return fileSystem.newWatchService();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
