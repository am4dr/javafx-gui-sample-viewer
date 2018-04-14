package com.gihtub.am4dr.javafx.sample_viewer.sample;

import com.gihtub.am4dr.javafx.sample_viewer.Sample;
import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

import java.net.URLClassLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class UpdateAwareSample<R extends Node> extends Sample<R> implements Sample.Reloadable {

    private final UpdateAwareNode<R> node;

    public UpdateAwareSample(String title, Class<R> rootClass, Supplier<URLClassLoader> cls) {
        this(title, rootClass.getName(), cls);
    }
    public UpdateAwareSample(String title, Class<R> rootClass, Supplier<URLClassLoader> cls, Consumer<? super R> initializer) {
        this(title, rootClass.getName(), cls, initializer);
    }
    public UpdateAwareSample(String title, String rootClassName, Supplier<URLClassLoader> cls) {
        this(title, rootClassName, cls, r -> {});
    }
    public UpdateAwareSample(String title, String rootClassName, Supplier<URLClassLoader> cls, Consumer<? super R> initializer) {
        super(title, initializer);
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
