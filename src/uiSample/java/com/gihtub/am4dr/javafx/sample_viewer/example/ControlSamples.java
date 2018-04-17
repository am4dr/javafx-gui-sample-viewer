package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleCollection;
import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleCollectionViewer;
import javafx.scene.Scene;
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
        samples.addSample("SampleViewerSample by Class<T> class", new UpdateAwareNode<>(this::createWatcher, SampleViewerSample.class));
        samples.addSample("SampleViewerSample by class name", new UpdateAwareNode<>(this::createWatcher, SampleViewerSample.class.getName()));
        return samples;
    }
}
