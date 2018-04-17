package com.gihtub.am4dr.javafx.sample_viewer.ui;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import java.util.function.Consumer;

import static javafx.collections.FXCollections.observableArrayList;

public final class SampleCollectionViewer {

    private final SampleCollection samples;
    private final View view;

    public SampleCollectionViewer(SampleCollection samples) {
        this.samples = samples;
        this.view = createView(samples);
    }

    public static View createView(SampleCollection samples) {
        final View view = new View();
        view.onNthTitleClicked.set(samples::select);
        view.content.bind(samples.selectedNode);
        view.titles.bind(samples.titles);
        return view;
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
