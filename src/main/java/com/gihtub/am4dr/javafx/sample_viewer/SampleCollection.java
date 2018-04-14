package com.gihtub.am4dr.javafx.sample_viewer;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;

public final class SampleCollection {

    public final ReadOnlyProperty<ObservableList<String>> titles = new SimpleListProperty<>(observableArrayList());
    public final ReadOnlyObjectProperty<Sample<?>> selected = new SimpleObjectProperty<>();
    public final ReadOnlyObjectProperty<Node> selectedNode = new SimpleObjectProperty<>();

    private final ObservableList<Sample<?>> samples = observableArrayList();

    public SampleCollection() {
        selected.addListener((o, old, now) -> ((SimpleObjectProperty<Node>)selectedNode).bind(now.nodeProperty()));
        samples.addListener((Observable o) -> {
            ((SimpleListProperty<String>)titles).setAll(samples.stream().map(it -> it.title).collect(Collectors.toList()));
            if (samples.size() == 1) {
                select(0);
            }
        });
    }

    public <R extends Node> void addSample(Sample<R> sample) {
        if (sample.getNode() != null) {
            samples.add(sample);
        }
    }

    public void select(int i) {
        if (i >= 0 && i < samples.size()) {
            ((SimpleObjectProperty<Sample<?>>)selected).set(samples.get(i));
        }
    }
}
