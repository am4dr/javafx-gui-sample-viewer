package com.github.am4dr.javafx.sample_viewer.internal;

import com.github.am4dr.javafx.sample_viewer.PathWatchEvent;

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
        final var absolutePath = path.normalize().toAbsolutePath();
        registeredPaths.add(absolutePath);
        getAvailableTargetPaths(path).forEach(this::register);
    }

    private void register(Path path) {
        try {
            final var key = path.register(watchService, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
            watchedKeys.add(key);
        } catch (IOException | ClosedWatchServiceException e) {
            e.printStackTrace();
        }
    }
    private static List<Path> getAvailableTargetPaths(Path path) {
        final var paths = new ArrayList<Path>();
        if (Files.notExists(path) || !Files.isDirectory(path)) {
            getNearestParent(path).ifPresent(it -> {
                paths.add(it);
                paths.addAll(getAllParents(it));
            });
            return paths;
        }
        paths.add(path);
        paths.addAll(getAllChildren(path));
        paths.addAll(getAllParents(path));
        return paths;
    }
    private static List<Path> getAllChildren(Path path) {
        try {
            return Files.walk(path).filter(it -> Files.isDirectory(it)).collect(toList());
        } catch (IOException e) {
            return List.of();
        }
    }
    private static List<Path> getAllParents(Path path) {
        final var parent = path.getParent();
        if (parent == null) { return List.of(); }
        else {
            final var paths = new ArrayList<Path>();
            paths.add(parent);
            paths.addAll(getAllParents(parent));
            return paths;
        }
    }
    private static Optional<Path> getNearestParent(Path path) {
        return Optional.ofNullable(path.getParent())
                .flatMap(parent -> Files.exists(parent) ? Optional.of(parent) : getNearestParent(parent));
    }

    @Override
    public void watchBlocking(Consumer<List<PathWatchEvent>> eventListener) {
        while (!Thread.currentThread().isInterrupted()) {
            final WatchKey key;
            final List<PathWatchEvent> events;
            try {
                key = watchService.take();
                events = key.pollEvents().stream()
                        .map(e -> new PathWatchEvent(e, (Path)key.watchable()))
                        .collect(toList());
            } catch (InterruptedException e) {
                break;
            }

            events.forEach(event -> {
                if (event.kind == ENTRY_CREATE) {
                    final var path = event.path;
                    if (registeredPaths.isParent(path)) {
                        register(path);
                    }
                    else if (registeredPaths.contains(path)) {
                        getAvailableTargetPaths(path).forEach(this::register);
                    }
                    else if (Files.isDirectory(path) && registeredPaths.isChild(path)) {
                        getAllChildren(path).forEach(this::register);
                    }
                }
                if (event.kind == OVERFLOW) {
                    // TODO implement
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

        boolean contains(Path path) {
            return paths.stream().anyMatch(registeredPath -> registeredPath.startsWith(path) && path.startsWith(registeredPath));
        }
        boolean isParent(Path path) {
            return paths.stream().anyMatch(registeredPath -> registeredPath.getParent().startsWith(path));
        }
        boolean isChild(Path path) {
            return paths.stream().anyMatch(registeredPath -> path.getParent().startsWith(registeredPath));
        }
    }
}
