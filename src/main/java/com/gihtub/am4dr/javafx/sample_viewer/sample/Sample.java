package com.gihtub.am4dr.javafx.sample_viewer.sample;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

import java.util.function.Consumer;

/**
 *
 * @param <R> root type of the sample node tree
 */
public abstract class Sample<R extends Node> {

    public final Consumer<? super R> initializer;
    protected final ReadOnlyObjectWrapper<R> node = new ReadOnlyObjectWrapper<>();

    public Sample(Consumer<? super R> initializer) {
        this.initializer = initializer;
    }
    public Sample() {
        this(node -> {});
    }

    public abstract ReadOnlyObjectProperty<R> nodeProperty();

    public R getNode() {
        return nodeProperty().get();
    }

    public interface Reloadable {
        void reload();
    }
}
