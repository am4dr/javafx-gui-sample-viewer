package com.github.am4dr.javafx.sample_viewer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PathWatcherImplTest {

    private Path testDir;
    private PathWatcher watcher;
    private WatchService watchService;
    private EventCollector eventCollector;
    private Thread watcherThread;

    @BeforeEach
    void beforeEach(@TempDir Path tempDir) throws IOException {
        testDir = tempDir;
        watchService = testDir.getFileSystem().newWatchService();
        watcher = new PathWatcherImpl(watchService);
        eventCollector = new EventCollector();
        watcherThread = new Thread(() -> watcher.watchBlocking(eventCollector));
    }

    @AfterEach
    void afterEach() throws IOException {
        watcherThread.interrupt();
        watchService.close();
    }

    @Test
    void testTest() {
        assertTrue(true);
    }

    @Test
    void detectFileCreationTest() throws IOException, InterruptedException {
        watcherThread.start();
        watcher.addRecursively(testDir);

        final var newFile = testDir.resolve("newFile");
        Files.createFile(newFile);

        Thread.sleep(50);
        assertTrue(containsSameFile(eventCollector.flatPaths(), newFile));
    }

    @Test
    void detectFileCreationInSubDirectory() throws IOException, InterruptedException {
        watcherThread.start();
        final var newFile = testDir.resolve("subDir/newFile");
        Files.createDirectory(newFile.getParent());
        watcher.addRecursively(testDir);

        Files.createFile(newFile);

        Thread.sleep(50);
        assertTrue(containsSameFile(eventCollector.flatPaths(), newFile));
    }

    @Test
    void registerSubDirectoryWhileRunningTest() throws IOException, InterruptedException {
        watcherThread.start();
        watcher.addRecursively(testDir);

        final var subDir = testDir.resolve("subDir");
        Files.createDirectory(subDir);

        Thread.sleep(50);
        assertTrue(containsSameFile(watcher.getWatchedPaths(), subDir));
    }


    private static boolean containsSameFile(List<Path> paths, Path path) {
        return paths.stream().anyMatch(it -> {
            try {
                return Files.isSameFile(it, path);
            } catch (IOException e) {
                return false;
            }
        });
    }

    static class EventCollector implements Consumer<List<PathWatcher.PathWatchEvent>> {
        List<List<PathWatcher.PathWatchEvent>> eventLists = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void accept(List<PathWatcher.PathWatchEvent> pathWatchEvents) {
            eventLists.add(pathWatchEvents);
        }

        List<PathWatcher.PathWatchEvent> flatEvents() {
            return eventLists.stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        List<Path> flatPaths() {
            return eventLists.stream()
                    .flatMap(Collection::stream)
                    .map(it -> it.path)
                    .collect(Collectors.toList());
        }
    }
}