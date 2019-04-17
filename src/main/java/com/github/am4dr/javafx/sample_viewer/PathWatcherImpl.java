package com.github.am4dr.javafx.sample_viewer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.stream.Collectors.toList;

public final class PathWatcherImpl implements PathWatcher {

    private final WatchService watchService;
    private final PathCollection registeredPaths = new PathCollection();
    private final Set<WatchKey> watchedKeys = Collections.synchronizedSet(new HashSet<>());

    public PathWatcherImpl(WatchService watchService) {
        this.watchService = watchService;
    }


    @Override
    public List<Path> getWatchedPaths() {
        return watchedKeys.stream()
                .filter(WatchKey::isValid)
                .map(it -> (Path) it.watchable())
                .collect(toList());
    }

    @Override
    public void addRecursively(Path path) {
        final var absolutePath = path.toAbsolutePath();
        registeredPaths.add(absolutePath);
        getTargetPaths(absolutePath).forEach(this::register);
    }

    private void register(Path path) {
        try {
            final var key = path.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
            watchedKeys.add(key);
        } catch (IOException | ClosedWatchServiceException e) {
            e.printStackTrace();
        }
    }

    private static List<Path> getTargetPaths(Path path) {
        if (Files.notExists(path) || !Files.isDirectory(path)) {
            return getNearestParent(path).stream().collect(toList());
        }
        try {
            return Files.walk(path).filter(it -> Files.isDirectory(it)).collect(toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    private static Optional<Path> getNearestParent(Path path) {
        return Optional.ofNullable(path.getParent())
                .flatMap(parent -> Files.exists(parent) ? Optional.of(parent) : getNearestParent(parent));
    }

    @Override
    public void watchBlocking(Consumer<List<PathWatcher.PathWatchEvent>> eventListener) {
        while (!Thread.currentThread().isInterrupted()) {
            final WatchKey key;
            final List<PathWatcher.PathWatchEvent> events;
            try {
                key = watchService.take();
                events = key.pollEvents().stream()
                        .map(e -> new PathWatcher.PathWatchEvent(e, (Path)key.watchable()))
                        .collect(toList());
            } catch (InterruptedException e) {
                break;
            }

            events.forEach(event -> {
                if (event.kind == ENTRY_CREATE) {
                    if (registeredPaths.isParent(event.path)) {
                        register(event.path);
                    }
                    if (Files.isDirectory(event.path) && registeredPaths.isChild(event.path)) {
                        register(event.path);
                    }
                }
            });
            eventListener.accept(events);

            final var keyIsValid = key.reset();
            if (!keyIsValid) {
                watchedKeys.remove(key);
            }
        }
    }



    static final class PathCollection {

        private final Set<Path> paths = Collections.synchronizedSet(new HashSet<>());

        void add(Path path) {
            paths.add(path);
        }

        boolean isParent(Path path) {
            return paths.stream().anyMatch(registeredPath -> registeredPath.getParent().startsWith(path));
        }
        boolean isChild(Path path) {
            return paths.stream().anyMatch(registeredPath -> path.getParent().startsWith(registeredPath));
        }
    }

}
