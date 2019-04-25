package com.github.am4dr.javafx.sample_viewer.example;

import com.github.am4dr.javafx.sample_viewer.NodeLatestInstanceBinding;
import com.github.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import com.github.am4dr.javafx.sample_viewer.ui.SampleCollection;
import com.github.am4dr.javafx.sample_viewer.ui.SampleCollectionViewer;
import com.github.am4dr.javafx.sample_viewer.ui.StatusBorderedPane;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class ControlSamples extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {
        final SampleCollectionViewer viewer = new SampleCollectionViewer(createSamples());
        stage.setTitle("GUI control samples of SampleViewer");
        stage.setScene(new Scene(viewer.getView(), 600.0, 400.0));
        stage.show();
    }

    private SampleCollection createSamples() {
        final SampleCollection samples = new SampleCollection();
        samples.addSample("empty SampleViewer.View", new SampleCollectionViewer.View());
        samples.addSample("SampleViewerSample by Class<T> class", new NodeLatestInstanceBinding(this::createClassLoader, SampleViewerSample.class));
        samples.addSample("SampleViewerSample by class name", new NodeLatestInstanceBinding(this::createClassLoader, SampleViewerSample.class.getName()));
        samples.addSample("BorderLayer", new StatusBorderedPane.BorderLayer());
        samples.addSample("StatusBorderedPane(OK)", new StatusBorderedPane(new NodeLatestInstanceBinding(this::createClassLoader, Pane.class)));
        samples.addSample("StatusBorderedPane(ERROR)", new StatusBorderedPane(new NodeLatestInstanceBinding(this::createClassLoader, "NotFound")));
        return samples;
    }
}
