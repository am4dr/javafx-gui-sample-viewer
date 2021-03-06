package com.github.am4dr.javafx.sample_viewer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUpdatePublisherTest {

    private Path testDir;
    private FileUpdatePublisher fileUpdatePublisher;

    @BeforeEach
    void beforeEach(@TempDir Path tempDir) {
        testDir = tempDir;

        fileUpdatePublisher = new FileUpdatePublisher();
    }

    @AfterEach
    void afterEach() {
        fileUpdatePublisher.shutdown();
    }

    @Test
    void fileUpdateTest() throws InterruptedException, IOException {
        fileUpdatePublisher.addDirectory(testDir);

        final FlowCollector<Path> collector = new FlowCollector<>();
        fileUpdatePublisher.subscribe(collector);


        final Path newFile = testDir.resolve("newFile");
        Files.createFile(newFile);

        Thread.sleep(100);
        final Optional<Path> last = collector.getLast();
        assertTrue(last.isPresent());
    }

    @Test
    void watchingNotExistDirectoryTest() throws IOException, InterruptedException {
        final FlowCollector<Path> collector = new FlowCollector<>();
        fileUpdatePublisher.subscribe(collector);

        final Path notFoundDir = Files.createDirectories(testDir.resolve("not-found"));
        fileUpdatePublisher.addDirectory(notFoundDir);
        Files.delete(notFoundDir);

        final Path recreated = Files.createDirectories(testDir.resolve("not-found"));
        fileUpdatePublisher.addDirectory(recreated);
        Files.createFile(recreated.resolve("newFle"));

        Thread.sleep(100);
        final Optional<Path> last = collector.getLast();
        assertTrue(last.isPresent());
    }

    @Test
    void addDirectoryTest() throws IOException {
        final Path file = Files.createFile(testDir.resolve("file"));
        assertThrows(IllegalArgumentException.class, () -> fileUpdatePublisher.addDirectory(file));
    }
}
