package com.gihtub.am4dr.javafx.sample_viewer;

import com.gihtub.am4dr.javafx.sample_viewer.sample.SampleNodeContainer;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;

public final class SampleViewer {

    private final View view = new View();
    private final SampleCollection samples = new SampleCollection();

    public SampleViewer() {
        view.onNthTitleClicked.set(samples::select);
        view.content.bind(samples.selectedNode);
        view.titles.bind(samples.titles);
    }

    public <R extends Node> void addSample(String title, SampleNodeContainer<R> sample) {
        samples.addSample(title, sample);
    }

    public View getView() {
        return view;
    }


    public static final class View extends BorderPane {

        public final ObjectProperty<Consumer<Integer>> onNthTitleClicked = new SimpleObjectProperty<>(i -> {});
        public final ListProperty<String> titles = new SimpleListProperty<>(observableArrayList());
        public final ObjectProperty<Node> content = centerProperty();

        private final ListView<String> listView = new ListView<>();

        public View() {
            listView.setPrefWidth(200.0);
            listView.itemsProperty().bind(titles);
            listView.setOnMouseClicked(e -> onNthTitleClicked.get().accept(listView.getSelectionModel().getSelectedIndex()));
            setLeft(listView);
        }
    }
}
