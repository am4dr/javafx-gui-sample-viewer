package com.gihtub.am4dr.javafx.sample_viewer;

import com.gihtub.am4dr.javafx.sample_viewer.util.DaemonThreadFactory;
import com.gihtub.am4dr.javafx.sample_viewer.util.SimpleSubscriber;
import com.gihtub.am4dr.javafx.sample_viewer.util.WaitLastProcessor;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.scene.Node;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedConsumer.uncheckedConsumer;
import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedRunnable.uncheckedRunnable;

public final class UpdateAwareNode<R extends Node> extends ObjectBinding<R> {

    private final String name;
    private final Supplier<URLClassLoader> cls;
    private final Consumer<? super R> initializer;
    private R node;

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
        final URLClassLoader newLoader = newClassLoader();
        createNode(newLoader).ifPresentOrElse(n -> {
            getCurrentNodeClassLoader().ifPresent(uncheckedConsumer(URLClassLoader::close));
            this.node = n;
        }, uncheckedRunnable(newLoader::close));
        return this.node;
    }

    private URLClassLoader newClassLoader() {
        final URLClassLoader loader = cls.get();
        watchReloadEvent(loader);
        return loader;
    }

    private Optional<R> createNode(ClassLoader loader) {
        try {
            final R node = (R) loader.loadClass(name).getDeclaredConstructor().newInstance();
            initializer.accept(node);
            return Optional.of(node);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            // TODO Change state
        }
        return Optional.empty();
    }

    private Optional<URLClassLoader> getCurrentNodeClassLoader() {
        return Optional.ofNullable(node)
                .map(R::getClass).map(Class::getClassLoader)
                .map(URLClassLoader.class::cast);
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
                Platform.runLater(UpdateAwareNode.this::invalidate);
                subscription.request(1);
            }
        });
        ((ClassPathWatcher) loader).getChangePublisher().subscribe(lastProcessor);
    }
}
