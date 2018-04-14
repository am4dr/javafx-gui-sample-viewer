package com.gihtub.am4dr.javafx.sample_viewer.sample;

import com.gihtub.am4dr.javafx.sample_viewer.ClassPathWatcher;
import com.gihtub.am4dr.javafx.sample_viewer.Sample;
import com.gihtub.am4dr.javafx.sample_viewer.util.DaemonThreadFactory;
import com.gihtub.am4dr.javafx.sample_viewer.util.SimpleSubscriber;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class NameBasedSample<R extends Node> extends Sample<R> implements Sample.Reloadable {

    public final String name;
    public final Class<R> rootClass;

    private final Supplier<URLClassLoader> cls;
    private URLClassLoader cl;

    public NameBasedSample(String title, String name, Class<R> rootClass, Supplier<URLClassLoader> cls) {
        super(title);
        this.name = name;
        this.rootClass = rootClass;
        this.cls = cls;
    }
    public NameBasedSample(String title, String name, Class<R> rootClass, Supplier<URLClassLoader> cls, Consumer<? super R> initializer) {
        super(title, initializer);
        this.name = name;
        this.rootClass = rootClass;
        this.cls = cls;
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.INSTANCE);

    private void watchReloadEvent(URLClassLoader loader) {
        if (!(loader instanceof ClassPathWatcher)) {
            return;
        }
        final ClassPathWatcher watcher = (ClassPathWatcher) loader;
        final Flow.Publisher<Path> changePublisher = watcher.getChangePublisher();
        changePublisher.subscribe(new SimpleSubscriber<>() {
            Future<?> update;
            @Override
            public void onNext(Path item) {
                if (update != null) {
                    update.cancel(true);
                }
                update = executor.schedule(() -> {
                    subscription.cancel();
                    Platform.runLater(NameBasedSample.this::reload);
                }, 1000, TimeUnit.MILLISECONDS);
                subscription.request(1);
            }
        });
    }

    @Override
    public ReadOnlyObjectProperty<R> nodeProperty() {
        if (node.get() == null) {
            node.set(createNode());
        }
        return node.getReadOnlyProperty();
    }

    private R createNode() {
        try {
            if (cl == null) {
                cl = cls.get();
                watchReloadEvent(cl);
            }
            var newNode = (R) cl.loadClass(name).getDeclaredConstructor().newInstance();
            initializer.accept(newNode);
            return newNode;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reload() {
        try {
            node.set(null);
            cl.close();
            cl = null;
            node.set(createNode());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
