package com.gihtub.am4dr.javafx.sample_viewer.ui;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.stream.Collectors;

import static javafx.collections.FXCollections.observableArrayList;

public final class SampleCollection {

    public final ReadOnlyProperty<ObservableList<String>> titles = new SimpleListProperty<>(observableArrayList());
    public final ReadOnlyObjectProperty<ObservableValue<? extends Node>> selected = new SimpleObjectProperty<>();
    public final ReadOnlyObjectProperty<Node> selectedNode = new SimpleObjectProperty<>();

    private final ObservableList<Entry> entries = observableArrayList();

    public SampleCollection() {
        selected.addListener((o, old, now) -> ((SimpleObjectProperty<Node>)selectedNode).bind(now));
        entries.addListener((Observable o) -> {
            ((SimpleListProperty<String>)titles).setAll(entries.stream().map(it -> it.title).collect(Collectors.toList()));
            if (entries.size() == 1) {
                select(0);
            }
        });
    }

    public <R extends Node> void addSample(String title, ObservableValue<R> sample) {
        if (sample.getValue() != null) {
            entries.add(new Entry(title, sample));
        }
    }

    public void select(int i) {
        if (i >= 0 && i < entries.size()) {
            ((SimpleObjectProperty<ObservableValue<? extends Node>>)selected).set(entries.get(i).sample);
        }
    }

    private static class Entry {

        public final String title;
        public final ObservableValue<? extends Node> sample;

        private Entry(String title, ObservableValue<? extends Node> sample) {
            this.title = title;
            this.sample = sample;
        }
    }
}
