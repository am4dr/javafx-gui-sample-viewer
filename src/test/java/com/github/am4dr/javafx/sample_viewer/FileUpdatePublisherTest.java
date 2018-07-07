package com.github.am4dr.javafx.sample_viewer;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.common.jimfs.WatchServiceConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUpdatePublisherTest {

    private FileSystem jimfs;
    private Path testDir;
    private FileUpdatePublisher fileUpdatePublisher;

    @BeforeEach
    void beforeEach() throws IOException {
        final Configuration jimfsConfig = Configuration.unix().toBuilder()
                .setWatchServiceConfiguration(WatchServiceConfiguration.polling(50, TimeUnit.MILLISECONDS))
                .build();
        jimfs = Jimfs.newFileSystem(jimfsConfig);
        testDir = jimfs.getPath("test");
        Files.createDirectories(testDir);
        fileUpdatePublisher = new FileUpdatePublisher();
    }

    @AfterEach
    void afterEach() {
        fileUpdatePublisher.shutdown();
    }

    @Test
    void fileUpdateTest() throws InterruptedException, IOException {
        fileUpdatePublisher.addDirectory(testDir);

        final FlowCollector<Path> collector = new FlowCollector<>(Collections.synchronizedList(new ArrayList<>()));
        fileUpdatePublisher.subscribe(collector);


        final Path newFile = testDir.resolve("newFile");
        Files.createFile(newFile);

        Thread.sleep(100);
        final Optional<Path> last = collector.getLast();
        assertTrue(last.isPresent());
    }

    @Test
    void addDirectoryTest() throws IOException {
        final Path file = Files.createFile(testDir.resolve("file"));
        assertThrows(IllegalArgumentException.class, () -> fileUpdatePublisher.addDirectory(file));
    }

    static class FlowCollector<T> implements Flow.Subscriber<T> {

        private final List<T> list;
        private Flow.Subscription subscription;

        public FlowCollector(List<T> list) {
            this.list = list;
        }

        public List<T> getList() {
            return Collections.unmodifiableList(list);
        }

        public Optional<T> getLast() {
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size()-1));
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            list.add(item);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
        }

        @Override
        public void onComplete() {
        }
    }
}
