package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedConsumer.uncheckedConsumer;
import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedFunction.uncheckedFunction;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public final class UpdateAwareURLClassLoader extends URLClassLoader implements ReportingClassLoader {

    private final PathWatchEventPublisher publisher;
    private final SubmissionPublisher<Path> createEventPublisher = new SubmissionPublisher<>();
    private final SubmissionPublisher<Path> loadedPathPublisher = new SubmissionPublisher<>();
    private final List<Path> watchPaths;

    public UpdateAwareURLClassLoader() {
        this(List.of());
    }
    public UpdateAwareURLClassLoader(List<Path> paths) {
        this(paths, List.of());
    }
    public UpdateAwareURLClassLoader(List<Path> watchPaths, List<Path> loadOnlyPaths) {
        super(new URL[0]);
        Stream.concat(watchPaths.stream(), loadOnlyPaths.stream())
                .peek(uncheckedConsumer(it -> { if (Files.notExists(it)) {
                    Files.createDirectories(it);
                }}))
                .map(uncheckedFunction(path -> path.toUri().toURL()))
                .forEach(this::addURL);
        this.watchPaths = watchPaths.stream().map(uncheckedFunction(Path::toRealPath)).collect(Collectors.toList());
        publisher = new PathWatchEventPublisher();
        publisher.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(List<PathWatchEvent> item) {
                item.stream()
                        .filter(it -> it.kind == ENTRY_CREATE)
                        .map(it -> it.path)
                        .forEach(createEventPublisher::submit);
                subscription.request(1);
            }
        });
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
                // call super class implementation to delegate to this parent ClassLoader
                return super.loadClass(name);
            }
            try {
                final Path loadedClassFilePath = Paths.get(aClass.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .resolve(aClass.getName().replace(".", "/") + ".class")
                        .toRealPath();
                if (watchPaths.stream().anyMatch(loadedClassFilePath::startsWith)) {
                    loadedPathPublisher.submit(loadedClassFilePath);
                    publisher.addRecursively(loadedClassFilePath);
                }
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
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



    @Override
    public Flow.Publisher<Path> getLoadedPathPublisher() {
        return loadedPathPublisher;
    }

    @Override
    public Optional<Class<?>> load(String fqcn) {
        try {
            return Optional.ofNullable(loadClass(fqcn));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        try {
            close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
