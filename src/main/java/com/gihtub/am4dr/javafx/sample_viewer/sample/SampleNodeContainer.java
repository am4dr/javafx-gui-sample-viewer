package com.gihtub.am4dr.javafx.sample_viewer.sample;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

/**
 *
 * @param <R> root type of the sample node tree
 */
public interface SampleNodeContainer<R extends Node> {

    ReadOnlyObjectProperty<R> nodeProperty();

    default R getNode() {
        return nodeProperty().get();
    }
}
