package com.gihtub.am4dr.javafx.sample_viewer.sample;

import javafx.scene.Node;

public final class SimpleSample<T extends Node> extends SampleNodeGenerator<T> {

    public SimpleSample(T node) {
        super.node.set(node);
    }
}
