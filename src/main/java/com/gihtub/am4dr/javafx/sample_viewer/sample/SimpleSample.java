package com.gihtub.am4dr.javafx.sample_viewer.sample;

import com.gihtub.am4dr.javafx.sample_viewer.Sample;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

public final class SimpleSample<T extends Node> extends Sample<T> {

    public SimpleSample(String title, T node) {
        super(title);
        super.node.set(node);
    }

    @Override
    public ReadOnlyObjectProperty<T> nodeProperty() {
        return super.node.getReadOnlyProperty();
    }
}
