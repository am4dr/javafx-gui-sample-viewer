package com.gihtub.am4dr.javafx.sample_viewer.sample;

import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

import java.net.URLClassLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UpdateAwareSample<R extends Node> extends Sample<R> implements Sample.Reloadable {

    private final UpdateAwareNode<R> node;

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
        super(initializer);
        this.node = new UpdateAwareNode<R>(cls, rootClassName, super.initializer);
        super.node.bind(this.node);
    }

    @Override
    public ReadOnlyObjectProperty<R> nodeProperty() {
        return super.node.getReadOnlyProperty();
    }

    @Override
    public void reload() {
        node.invalidate();
    }
}
