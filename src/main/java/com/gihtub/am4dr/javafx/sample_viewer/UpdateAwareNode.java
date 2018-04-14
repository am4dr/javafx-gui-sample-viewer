package com.gihtub.am4dr.javafx.sample_viewer;

import com.gihtub.am4dr.javafx.sample_viewer.util.DaemonThreadFactory;
import com.gihtub.am4dr.javafx.sample_viewer.util.SimpleSubscriber;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class UpdateAwareNode<R extends Node> extends ObjectBinding<R> {

    private final Class<R> rootClass;
    private final Supplier<URLClassLoader> cls;
    private final Consumer<? super R> initializer;

    public UpdateAwareNode(Supplier<URLClassLoader> cls, Class<R> rootClass, Consumer<? super R> initializer) {
        this.rootClass = rootClass;
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
            final Object obj = cl.loadClass(rootClass.getName()).getDeclaredConstructor().newInstance();
            var newNode = (R) obj;
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
        final ClassPathWatcher watcher = (ClassPathWatcher) loader;
        final Flow.Publisher<Path> changePublisher = watcher.getChangePublisher();
        changePublisher.subscribe(new SimpleSubscriber<>() {
            Future<?> reloadFuture;
            @Override
            public void onNext(Path item) {
                if (reloadFuture != null) {
                    reloadFuture.cancel(true);
                }
                reloadFuture = executor.schedule(() -> {
                    subscription.cancel();
                    Platform.runLater(UpdateAwareNode.this::invalidate);
                }, 1000, TimeUnit.MILLISECONDS);
                subscription.request(1);
            }
        });
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
