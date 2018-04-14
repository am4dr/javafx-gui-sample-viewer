package com.gihtub.am4dr.javafx.sample_viewer;

import com.gihtub.am4dr.javafx.sample_viewer.util.DaemonThreadFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Collectors;

import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedFunction.uncheckedFunction;

public final class ClassPathWatcher extends URLClassLoader {

    private final List<Path> paths = new ArrayList<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE);
    private final SubmissionPublisher<Path> publisher = new SubmissionPublisher<>(executor, 10);
    private final WatchService watchService;

    public ClassPathWatcher() {
        this(List.of());
    }
    public ClassPathWatcher(List<Path> paths) {
        super(new URL[0]);
        paths.stream().map(uncheckedFunction(path -> path.toUri().toURL())).forEach(this::addURL);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            take(watchService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void take(WatchService watchService) {
        executor.submit(() -> {
            while (!Thread.interrupted()) {
                final WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    break;
                }
                final List<Path> changed = key.pollEvents().stream()
                        .map(it -> (Path) it.context())
                        .filter(it -> {
                            final String fileName = it.getFileName().toString();
                            return fileName.endsWith(".jar") || fileName.endsWith(".class");
                        })
                        .collect(Collectors.toList());
                if (!changed.isEmpty()) {
                    publisher.submit(changed.get(0));
                }
                key.reset();
            }
        });
    }

    @Override
    public void close() throws IOException {
        super.close();
        publisher.close();
        executor.shutdownNow();
        watchService.close();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            if (findLoadedClass(name) != null) {
                return super.loadClass(name);
            }
            Class<?> aClass = findClassOrNull(name);
            if (aClass == null) {
                aClass = super.loadClass(name);
            }
            if (aClass.getClassLoader() == this) {
                try {
                    final Path path = Paths.get(aClass.getProtectionDomain().getCodeSource().getLocation().toURI())
                            .resolve(aClass.getName().replace(".", "/") + ".class")
                            .toRealPath();
                    final Path parent = path.getParent();
                    if (!paths.contains(parent)) {
                        paths.add(parent);
                        parent.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    }
                } catch (URISyntaxException | IOException e) {
                    e.printStackTrace();
                }
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
}
