package com.gihtub.am4dr.javafx.sample_viewer;

import com.gihtub.am4dr.javafx.sample_viewer.util.DaemonThreadFactory;
import com.gihtub.am4dr.javafx.sample_viewer.util.SimpleSubscriber;
import com.gihtub.am4dr.javafx.sample_viewer.util.WaitLastProcessor;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UpdateAwareNode<R extends Node> extends ObjectBinding<R> {

    private final String name;
    private final Supplier<URLClassLoader> cls;
    private final Consumer<? super R> initializer;

    public UpdateAwareNode(Supplier<URLClassLoader> cls, Class<R> rootClass, Consumer<? super R> initializer) {
        this.name = rootClass.getName();
        this.cls = cls;
        this.initializer = initializer;
    }
    public UpdateAwareNode(Supplier<URLClassLoader> cls, String rootClassName, Consumer<? super R> initializer) {
        this.name = rootClassName;
        this.cls = cls;
        this.initializer = initializer;
    }

    @Override
    protected R computeValue() {
        return createNode();
    }

    private URLClassLoader cl;
    private R createNode() {
        try {
            if (cl == null) {
                cl = cls.get();
                watchReloadEvent(cl);
            }
            final R newNode = (R) cl.loadClass(name).getDeclaredConstructor().newInstance();
            initializer.accept(newNode);
            return newNode;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.INSTANCE);
    private void watchReloadEvent(URLClassLoader loader) {
        if (!(loader instanceof ClassPathWatcher)) {
            return;
        }
        final WaitLastProcessor<Path> lastProcessor = new WaitLastProcessor<>(executor, 1000, TimeUnit.MILLISECONDS);
        lastProcessor.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(Path item) {
                subscription.cancel();
                Platform.runLater(UpdateAwareNode.this::invalidate);
            }
        });
        ((ClassPathWatcher) loader).getChangePublisher().subscribe(lastProcessor);
    }

    @Override
    protected void onInvalidating() {
        try {
            cl.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cl = null;
    }
}
