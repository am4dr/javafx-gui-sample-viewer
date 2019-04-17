package com.github.am4dr.javafx.sample_viewer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PathWatchEventPublisherTest {

    private PathWatcher watcher;
    private PathWatchEventPublisher publisher;

    @BeforeEach
    void beforeEach() {
        watcher = mock(PathWatcher.class);
    }
    @AfterEach
    void afterEach() {
        if (publisher != null) {
            publisher.shutdown();
        }
    }

    private void startAndPublish(List<PathWatcher.PathWatchEvent> events) {
        doAnswer(invocation -> {
            final Consumer<List<PathWatcher.PathWatchEvent>> callback = invocation.getArgument(0);
            callback.accept(events);
            return null;
        }).when(watcher).watchBlocking(any());
        publisher = new PathWatchEventPublisher(watcher);
    }


    @Test
    void emptyEventListPublishingTest() {
        startAndPublish(List.of());

        final var collector = new FlowCollector<List<PathWatcher.PathWatchEvent>>();
        publisher.subscribe(collector);

        assertTrue(collector.getLast().isEmpty());
    }
}