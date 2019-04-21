package com.github.am4dr.javafx.sample_viewer;

import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;
import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.github.am4dr.javafx.sample_viewer.LatestInstanceProvider.Status.LOAD_SUCCEEDED;

/**
 *
 *
 * This class call {@link Platform#runLater(Runnable)} internally.
 */
public final class NodeLatestInstanceBinding extends ObjectBinding<Node> {

    private static final int defaultWaitTimeMillis = 100;
    private final Consumer<Node> initializer;
    private final Map<String, Object> contextMap;
    private final LatestInstanceProvider instanceProvider;

    private Node node = null;


    public NodeLatestInstanceBinding(Supplier<ReportingClassLoader> cls, Class<?> rootClass) {
        this(cls, rootClass.getName());
    }
    public NodeLatestInstanceBinding(Supplier<ReportingClassLoader> cls, String rootClassName) {
        this(new Builder().classloader(cls).name(rootClassName));
    }
    private NodeLatestInstanceBinding(Builder builder) {
        final var checkedBuilder = builder.check();
        this.initializer = checkedBuilder.initializer;
        this.contextMap = checkedBuilder.contextMap;
        final var name = checkedBuilder.className;
        final var cls = checkedBuilder.classLoaderSupplier;
        final var waitTimeToDetermineTheLastEvent = checkedBuilder.waitTimeMillis;

        instanceProvider = new LatestInstanceProvider(name, cls, waitTimeToDetermineTheLastEvent);
        instanceProvider.getStatusPublisher().subscribe(new SimpleSubscriber<>() {
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
    public static NodeLatestInstanceBinding build(UnaryOperator<Builder> configuration) {
        return configuration.apply(new Builder()).build();
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
    protected Node computeValue() {
        if (node != null && status.get() == STATUS.OK) {
            return node;
        }
        if (instanceProvider.getStatus() != LOAD_SUCCEEDED) {
            return node;
        }

        instanceProvider.getInstance().ifPresent(instance -> {
            if (Node.class.isAssignableFrom(instance.getClass())) {
                node = (Node)instance;
                initializer.accept(node);
                if (node instanceof RestorableNode) {
                    ((RestorableNode)node).restore(contextMap);
                }
                status.set(STATUS.OK);
            }
        });
        return node;
    }


    public enum STATUS {
        OK, ERROR, RELOADING, UPDATE_DETECTED
    }

    public static final class Builder {

        private final String className;
        private final Supplier<ReportingClassLoader> classLoaderSupplier;
        private final Consumer<Node> initializer;
        private final int waitTimeMillis;
        private final Map<String, Object> contextMap;

        public Builder() {
            this(null, null, null, defaultWaitTimeMillis, Map.of());
        }
        public Builder(String className, Supplier<ReportingClassLoader> classLoaderSupplier, Consumer<Node> initializer, int waitTimeMillis, Map<String, Object> contextMap) {
            this.className = className;
            this.classLoaderSupplier = classLoaderSupplier;
            this.initializer = initializer;
            this.waitTimeMillis = waitTimeMillis;
            this.contextMap = contextMap;
        }

        public Builder check() {
            if (className == null) {
                throw new IllegalStateException("className must not be null: call name(String) or type(Class<? extends Node>)");
            }
            if (classLoaderSupplier == null) {
                throw new IllegalStateException("classLoaderSupplier must not be null: call classloader(Supplier<ReportingClassLoader>)");
            }
            if (initializer == null) {
                return initializer(node -> {});
            }
            if (waitTimeMillis < 0) {
                return waitTimeMillis(defaultWaitTimeMillis);
            }
            return this;
        }
        public NodeLatestInstanceBinding build() {
            return new NodeLatestInstanceBinding(this);
        }

        public Builder name(String className) {
            return new Builder(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder classloader(Supplier<ReportingClassLoader> classLoaderSupplier) {
            return new Builder(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder initializer(Consumer<Node> initializer) {
            return new Builder(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder waitTimeMillis(int waitTimeMillis) {
            return new Builder(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder context(Map<String, Object> contextMap) {
            return new Builder(className, classLoaderSupplier, initializer, waitTimeMillis, contextMap);
        }
        public Builder type(Class<? extends Node> type) {
            return name(type.getName());
        }
    }
}
