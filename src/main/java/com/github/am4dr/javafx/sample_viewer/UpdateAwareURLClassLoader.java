package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;

import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedConsumer.uncheckedConsumer;
import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedFunction.uncheckedFunction;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public final class UpdateAwareURLClassLoader extends URLClassLoader {

    private final PathWatchEventPublisher publisher;
    private final SubmissionPublisher<Path> createEventPublisher = new SubmissionPublisher<>();

    public UpdateAwareURLClassLoader() {
        this(List.of());
    }
    public UpdateAwareURLClassLoader(List<Path> paths) {
        this(paths, List.of());
    }
    public UpdateAwareURLClassLoader(List<Path> watchPaths, List<Path> loadOnlyPaths) {
        this(watchPaths, loadOnlyPaths, getDefaultWatchService());
    }
    public UpdateAwareURLClassLoader(List<Path> watchPaths, List<Path> loadOnlyPaths, WatchService watchService) {
        super(new URL[0]);
        Stream.concat(watchPaths.stream(), loadOnlyPaths.stream())
                .peek(uncheckedConsumer(it -> { if (Files.notExists(it)) {
                    Files.createDirectories(it);
                }}))
                .map(uncheckedFunction(path -> path.toUri().toURL()))
                .forEach(this::addURL);
        final var watcher = new PathWatcherImpl(watchService);
        publisher = new PathWatchEventPublisher(watcher);
        publisher.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(List<PathWatcher.PathWatchEvent> item) {
                item.stream()
                        .filter(it -> it.kind == ENTRY_CREATE)
                        .map(it -> it.path)
                        .forEach(createEventPublisher::submit);
                subscription.request(1);
            }
        });
        watchPaths.stream().map(uncheckedFunction(Path::toRealPath)).forEach(watcher::addRecursively);
    }
    private static WatchService getDefaultWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        publisher.shutdown();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            if (findLoadedClass(name) != null) {
                return super.loadClass(name);
            }
            final Class<?> aClass = findClassOrNull(name);
            if (aClass == null) {
                return super.loadClass(name);
            }
            return aClass;
        }
    }
    private Class<?> findClassOrNull(String name) {
        try {
            return findClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Flow.Publisher<Path> getChangePublisher() {
        return createEventPublisher;
    }

    @Deprecated(forRemoval = true, since = "4.4")
    public void updateWatchKeys() {
    }
}
