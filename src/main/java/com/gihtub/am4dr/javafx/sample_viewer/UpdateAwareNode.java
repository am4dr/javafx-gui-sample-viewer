package com.gihtub.am4dr.javafx.sample_viewer;

import com.gihtub.am4dr.javafx.sample_viewer.util.DaemonThreadFactory;
import com.gihtub.am4dr.javafx.sample_viewer.util.SimpleSubscriber;
import com.gihtub.am4dr.javafx.sample_viewer.util.WaitLastProcessor;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedConsumer.uncheckedConsumer;
import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedRunnable.uncheckedRunnable;

/**
 *
 *
 * This class may call {@link Platform#runLater(Runnable)} internally.
 * @param <R>
 */
public final class UpdateAwareNode<R extends Node> extends ObjectBinding<R> {

    private final String name;
    private final Supplier<URLClassLoader> cls;
    private final Consumer<? super R> initializer;
    private R node;

    public UpdateAwareNode(Supplier<URLClassLoader> cls, Class<R> rootClass) {
        this(cls, rootClass.getName());
    }
    public UpdateAwareNode(Supplier<URLClassLoader> cls, String rootClassName) {
        this.name = rootClassName;
        this.cls = cls;
        this.initializer = n -> {};
    }
    private UpdateAwareNode(Builder<R> builder) {
        this.name = builder.className;
        this.cls = builder.classLoaderSupplier;
        this.initializer = builder.initializer;
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
        super.onInvalidating();
        status.set(STATUS.RELOADING);
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
            status.set(STATUS.OK);
            return Optional.of(node);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            status.set(STATUS.ERROR);
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
        final Flow.Publisher<Path> changePublisher = ((ClassPathWatcher) loader).getChangePublisher();
        changePublisher.subscribe(lastProcessor);
        changePublisher.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(Path item) {
                Platform.runLater(() -> UpdateAwareNode.this.status.set(STATUS.UPDATE_AWARE));
                subscription.request(1);
            }
        });
    }


    public enum STATUS {
        OK, ERROR, RELOADING, UPDATE_AWARE
    }

    public static final class Builder<R extends Node> {

        public final String className;
        public final Supplier<URLClassLoader> classLoaderSupplier;
        public final Consumer<? super R> initializer;

        public Builder() {
            this(null, null, null);
        }
        public Builder(String className, Supplier<URLClassLoader> classLoaderSupplier, Consumer<? super R> initializer) {
            this.className = className;
            this.classLoaderSupplier = classLoaderSupplier;
            this.initializer = initializer;
        }

        public UpdateAwareNode<R> build() {
            if (className == null) {
                throw new IllegalStateException("className must not be null: call name(String) or type(Class<? extends Node>)");
            }
            if (classLoaderSupplier == null) {
                throw new IllegalStateException("classLoaderSupplier must not be null: call classloader(Supplier<URLCLassLoader>)");
            }
            final Builder<R> builder = new Builder<R>(className, classLoaderSupplier, Objects.requireNonNullElse(initializer, node -> {}));
            return new UpdateAwareNode<>(builder);
        }

        public Builder<R> name(String className) {
            return new Builder<R>(className, classLoaderSupplier, initializer);
        }
        public Builder<R> classloader(Supplier<URLClassLoader> classLoaderSupplier) {
            return new Builder<R>(className, classLoaderSupplier, initializer);
        }
        public Builder<R> initializer(Consumer<? super R> initializer) {
            return new Builder<R>(className, classLoaderSupplier, initializer);
        }
        public Builder<R> type(Class<? extends R> type) {
            return name(type.getName());
        }
    }
}
