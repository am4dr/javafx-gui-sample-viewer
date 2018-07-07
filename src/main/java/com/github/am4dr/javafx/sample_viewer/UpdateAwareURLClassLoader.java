package com.github.am4dr.javafx.sample_viewer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Flow;

import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedFunction.uncheckedFunction;

public final class UpdateAwareURLClassLoader extends URLClassLoader {

    private final FileUpdatePublisher publisher = new FileUpdatePublisher();

    public UpdateAwareURLClassLoader() {
        this(List.of());
    }
    public UpdateAwareURLClassLoader(List<Path> paths) {
        super(new URL[0]);
        paths.stream().map(uncheckedFunction(path -> path.toUri().toURL())).forEach(this::addURL);
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
            try {
                final Path path = Paths.get(aClass.getProtectionDomain().getCodeSource().getLocation().toURI())
                        .resolve(aClass.getName().replace(".", "/") + ".class")
                        .toRealPath();
                publisher.addDirectory(path.getParent());
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
}
