package com.github.am4dr.javafx.sample_viewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedConsumer.uncheckedConsumer;
import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedFunction.uncheckedFunction;

public final class UpdateAwareURLClassLoader extends URLClassLoader {

    private final FileUpdatePublisher publisher = new FileUpdatePublisher();

    private final List<Path> watchPaths;
    private final List<Path> loadOnlyPaths;

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
        this.loadOnlyPaths = loadOnlyPaths.stream().map(uncheckedFunction(Path::toRealPath)).collect(Collectors.toList());
    }

    @Override
    public void close() throws IOException {
        super.close();
        publisher.shutdown();
    }

    private Set<Path> watchedDirectories = Collections.synchronizedSet(new HashSet<>());
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
            try {
                final Path path = Paths.get(aClass.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .resolve(aClass.getName().replace(".", "/") + ".class")
                        .toRealPath();
                if (watchPaths.stream().anyMatch(path::startsWith)) {
                    watchedDirectories.add(path.getParent());
                    publisher.addDirectory(path.getParent());
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

    public Flow.Publisher<Path> getChangePublisher() {
        return publisher;
    }

    public void updateWatchKeys() {
        publisher.reactivateKeys();
    }
}
