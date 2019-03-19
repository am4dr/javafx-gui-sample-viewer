package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.DaemonThreadFactory;
import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;
import com.github.am4dr.javafx.sample_viewer.internal.WaitLastProcessor;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedConsumer.uncheckedConsumer;
import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedRunnable.uncheckedRunnable;

/**
 *
 *
 * This class may call {@link Platform#runLater(Runnable)} internally.
 */
public final class UpdateAwareNode<R extends Node> extends ObjectBinding<R> {

    private final String name;
    private final Supplier<URLClassLoader> cls;
    private final Consumer<? super R> initializer;
    private final Map<String, Object> contextMap;
    private R node;
    private final int waitTimeToDetermineTheLastEvent;
    private static final int defaultWaitTimeMillis = 100;

    public UpdateAwareNode(Supplier<URLClassLoader> cls, Class<R> rootClass) {
        this(cls, rootClass.getName());
    }
    public UpdateAwareNode(Supplier<URLClassLoader> cls, String rootClassName) {
        this(new Builder<R>().classloader(cls).name(rootClassName));
    }
    private UpdateAwareNode(Builder<R> builder) {
        final var checkedBuilder = builder.check();
        this.name = checkedBuilder.className;
        this.cls = checkedBuilder.classLoaderSupplier;
        this.initializer = checkedBuilder.initializer;
        this.waitTimeToDetermineTheLastEvent = checkedBuilder.waitTimeMillis;
        this.contextMap = checkedBuilder.contextMap;
    }
    public static <R extends Node> UpdateAwareNode<R> build(UnaryOperator<Builder<R>> configuration) {
        return configuration.apply(new Builder<>()).build();
    }

    private final ReadOnlyObjectWrapper<STATUS> status = new ReadOnlyObjectWrapper<>(STATUS.RELOADING);
    public ReadOnlyObjectProperty<STATUS> statusProperty() {
        return status.getReadOnlyProperty();
    }

    @Override
    protected void onInvalidating() {
        status.set(STATUS.RELOADING);
        super.onInvalidating();
    }

    @Override
    protected R computeValue() {
        if (node != null && status.get() == STATUS.OK) {
            return node;
        }

        final Class<R> rClass = loadClassByNewLoaderOrNull();
        if (rClass == null) {
            status.set(STATUS.ERROR);
            return node;
        }

        final var newClassLoader = rClass.getClassLoader();
        createNode(rClass).ifPresentOrElse(newNode -> {
            if ((newClassLoader instanceof UpdateAwareURLClassLoader)) {
                watchReloadEvent((UpdateAwareURLClassLoader) newClassLoader);
            }
            getCurrentNodeClassLoader().ifPresent(uncheckedConsumer(URLClassLoader::close));
            node = newNode;
            status.set(STATUS.OK);
        }, uncheckedRunnable(() -> {
            refreshWatchKeys();
            if (newClassLoader instanceof Closeable) {
                ((Closeable) newClassLoader).close();
            }
            status.set(STATUS.ERROR);
        }));
        return node;
    }

    private Class<R> loadClassByNewLoaderOrNull() {
        final URLClassLoader newLoader = cls.get();
        try {
            final Class<R> rClass = (Class<R>)newLoader.loadClass(name);
            // node must be loaded by the specified ClassLoader
            final var loadedByParentLoader = rClass.getClassLoader() != newLoader;
            if (loadedByParentLoader) {
                uncheckedRunnable(newLoader::close).run();
                refreshWatchKeys();
                return null;
            }
            return rClass;
        } catch (ClassNotFoundException e) {
            // nothing to do
        }
        return null;
    }

    private Optional<R> createNode(Class<R> clazz) {
        R node = null;
        try {
            node = clazz.getDeclaredConstructor().newInstance();
            initializer.accept(node);
            if (node instanceof RestorableNode) {
                ((RestorableNode)node).restore(contextMap);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // nothing to do
        }
        return Optional.ofNullable(node);
    }

    private Optional<URLClassLoader> getCurrentNodeClassLoader() {
        return Optional.ofNullable(node)
                .map(it -> (URLClassLoader)it.getClass().getClassLoader());
    }
    private void refreshWatchKeys() {
        getCurrentNodeClassLoader()
                .filter(it -> it instanceof UpdateAwareURLClassLoader)
                .ifPresent(it -> ((UpdateAwareURLClassLoader) it).updateWatchKeys());
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.INSTANCE);
    private void watchReloadEvent(UpdateAwareURLClassLoader loader) {
        final WaitLastProcessor<Path> lastProcessor = new WaitLastProcessor<>(executor, waitTimeToDetermineTheLastEvent, TimeUnit.MILLISECONDS);
        lastProcessor.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(Path item) {
                Platform.runLater(UpdateAwareNode.this::invalidate);
                subscription.request(1);
            }
        });
        final Flow.Publisher<Path> changePublisher = loader.getChangePublisher();
        changePublisher.subscribe(lastProcessor);
        changePublisher.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(Path item) {
                Platform.runLater(() -> UpdateAwareNode.this.status.set(STATUS.UPDATE_DETECTED));
                subscription.request(1);
            }
        });
    }


    public enum STATUS {
        OK, ERROR, RELOADING, UPDATE_DETECTED
    }

    public static final class Builder<R extends Node> {

        private final String className;
        private final Supplier<URLClassLoader> classLoaderSupplier;
        private final Consumer<? super R> initializer;
        private final int waitTimeMillis;
        private final Map<String, Object> contextMap;

        public Builder() {
            this(null, null, null, defaultWaitTimeMillis, Map.of());
        }
        public Builder(String className, Supplier<URLClassLoader> classLoaderSupplier, Consumer<? super R> initializer, int waitTimeMillis, Map<String, Object> contextMap) {
            this.className = className;
            this.classLoaderSupplier = classLoaderSupplier;
            this.initializer = initializer;
            this.waitTimeMillis = waitTimeMillis;
            this.contextMap = contextMap;
        }

        public Builder<R> check() {
            if (className == null) {
                throw new IllegalStateException("className must not be null: call name(String) or type(Class<? extends Node>)");
            }
            if (classLoaderSupplier == null) {
                throw new IllegalStateException("classLoaderSupplier must not be null: call classloader(Supplier<URLCLassLoader>)");
            }
            if (initializer == null) {
                return initializer(node -> {});
            }
            if (waitTimeMillis < 0) {
                return waitTimeMillis(defaultWaitTimeMillis);
            }
            return this;
        }
        public UpdateAwareNode<R> build() {
            return new UpdateAwareNode<>(this);
        }

        public Builder<R> name(String className) {
            return new Builder<R>(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder<R> classloader(Supplier<URLClassLoader> classLoaderSupplier) {
            return new Builder<R>(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder<R> initializer(Consumer<? super R> initializer) {
            return new Builder<R>(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder<R> waitTimeMillis(int waitTimeMillis) {
            return new Builder<R>(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder<R> context(Map<String, Object> contextMap) {
            return new Builder<R>(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder<R> type(Class<? extends R> type) {
            return name(type.getName());
        }
    }
}
