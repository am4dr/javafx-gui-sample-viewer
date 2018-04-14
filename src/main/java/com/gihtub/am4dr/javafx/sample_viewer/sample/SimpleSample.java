package com.gihtub.am4dr.javafx.sample_viewer.sample;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

public final class SimpleSample<T extends Node> extends Sample<T> {

    public SimpleSample(T node) {
        super.node.set(node);
    }

    @Override
    public ReadOnlyObjectProperty<T> nodeProperty() {
        return super.node.getReadOnlyProperty();
    }
}
