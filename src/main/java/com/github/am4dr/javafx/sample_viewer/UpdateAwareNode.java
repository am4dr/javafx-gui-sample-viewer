package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.DaemonThreadFactory;
import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.github.am4dr.javafx.sample_viewer.LatestInstanceProvider.Status.LOAD_SUCCEEDED;

/**
 *
 *
 * This class may call {@link Platform#runLater(Runnable)} internally.
 * TODO 0.4.5でインターフェースを維持したまま内部的にLatestInstanceProviderを使用するようにやや強引に書き換えたため、
 *      より適した新しいクラスを実装ののちに Deprecated をつける
 * TODO ジェネリクスをつかって実際のクラスをRとして取っているのは実行時には消えているとはいえソースが難しいのでやめる
 *      あくまでもObjectBinding(Node)にしておく
 */
public final class UpdateAwareNode<R extends Node> extends ObjectBinding<R> {

    private final String name;
    private final Supplier<URLClassLoader> cls;
    private final Consumer<? super R> initializer;
    private final Map<String, Object> contextMap;
    private R node;
    private final int waitTimeToDetermineTheLastEvent;
    private static final int defaultWaitTimeMillis = 100;

    // TODO Class<R>ではなくClass<?>をとるようにする
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


        instanceProvider = new LatestInstanceProvider(
                name,
                new ClassLoaderSupplierWrapper(cls),
                Executors.newCachedThreadPool(DaemonThreadFactory.INSTANCE),
                waitTimeToDetermineTheLastEvent);
        instanceProvider.getStatusPublisher.subscribe(new SimpleSubscriber<>() {
            @Override
            public void process(LatestInstanceProvider.Status item) {
                Platform.runLater(() -> {
                    switch (item) {
                        case CHANGE_DETECTED:
                            status.set(STATUS.UPDATE_DETECTED);
                            break;
                        case LOAD_FAILED:
                            status.set(STATUS.ERROR);
                            break;
                        case LOAD_SUCCEEDED:
                            invalidate();
                            break;
                    }
                });
            }
        });
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
    }

    @Override
    protected R computeValue() {
        if (node != null && status.get() == STATUS.OK) {
            return node;
        }
        if (instanceProvider.getStatus() != LOAD_SUCCEEDED) {
            return node;
        }

        instanceProvider.getInstance().ifPresent(instance -> {
            if (Node.class.isAssignableFrom(instance.getClass())) {
                node = (R)instance;
                initializer.accept(node);
                if (node instanceof RestorableNode) {
                    ((RestorableNode)node).restore(contextMap);
                }
                status.set(STATUS.OK);
            }
        });
        return node;
    }



    private final LatestInstanceProvider instanceProvider;
    private class ClassLoaderSupplierWrapper implements Supplier<ReportingClassLoader> {

        private final Supplier<URLClassLoader> cls;

        public ClassLoaderSupplierWrapper(Supplier<URLClassLoader> cls) {
            this.cls = cls;
        }

        @Override
        public ReportingClassLoader get() {
            final var urlClassLoader = cls.get();
            if (ReportingClassLoader.class.isAssignableFrom(urlClassLoader.getClass())) {
                return (ReportingClassLoader) urlClassLoader;
            }
            else {
                return new ReportingClassLoader() {

                    private final SubmissionPublisher<Path> pathSubmissionPublisher = new SubmissionPublisher<>();

                    @Override
                    public Flow.Publisher<Path> getLoadedPathPublisher() {
                        return pathSubmissionPublisher;
                    }

                    @Override
                    public Optional<Class<?>> load(String fqcn) {
                        try {
                            return Optional.ofNullable(urlClassLoader.loadClass(fqcn));
                        } catch (ClassNotFoundException e) {
                            return Optional.empty();
                        }
                    }

                    @Override
                    public void shutdown() {
                        pathSubmissionPublisher.close();
                        try {
                            urlClassLoader.close();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                };
            }
        }
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
