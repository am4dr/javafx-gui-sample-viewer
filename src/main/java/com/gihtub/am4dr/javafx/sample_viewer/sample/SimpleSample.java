package com.gihtub.am4dr.javafx.sample_viewer.sample;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;

public final class SimpleSample<R extends Node> implements SampleNodeContainer<R> {

    private final ReadOnlyObjectWrapper<R> node;

    public SimpleSample(R node) {
        this.node = new ReadOnlyObjectWrapper<>(node);
    }

    @Override
    public ReadOnlyObjectProperty<R> nodeProperty() {
        return node.getReadOnlyProperty();
    }
}
