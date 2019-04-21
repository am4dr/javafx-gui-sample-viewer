package com.github.am4dr.javafx.sample_viewer.internal;

import com.github.am4dr.javafx.sample_viewer.PathWatchEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * TODO 実質的PathWatchEventPublisherのテスト用のインターフェースになっているので削除する
 */
public interface PathWatcher {

    List<Path> getWatchedPaths();
    void addRecursively(Path path);
    void watchBlocking(Consumer<List<PathWatchEvent>> eventListener);
}
