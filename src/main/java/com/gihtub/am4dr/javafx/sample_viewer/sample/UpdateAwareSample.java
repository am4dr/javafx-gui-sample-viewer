package com.gihtub.am4dr.javafx.sample_viewer.sample;

import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.net.URLClassLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UpdateAwareSample<R extends Node> implements SampleNodeContainer<R> {

    private final ReadOnlyObjectWrapper<R> node = new ReadOnlyObjectWrapper<>();

    public UpdateAwareSample(Supplier<URLClassLoader> cls, Class<R> rootClass) {
        this(cls, rootClass.getName());
    }
    public UpdateAwareSample(Supplier<URLClassLoader> cls, Class<R> rootClass, Consumer<? super R> initializer) {
        this(cls, rootClass.getName(), initializer);
    }
    public UpdateAwareSample(Supplier<URLClassLoader> cls, String rootClassName) {
        this(cls, rootClassName, r -> {});
    }
    public UpdateAwareSample(Supplier<URLClassLoader> cls, String rootClassName, Consumer<? super R> initializer) {
        final UpdateAwareNode<R> node = UpdateAwareNode.<R>builder()
                .name(rootClassName)
                .classloader(cls)
                .initializer(initializer)
                .build();
        this.node.bind(node);
    }

    @Override
    public ReadOnlyObjectProperty<R> nodeProperty() {
        return node.getReadOnlyProperty();
    }
}
