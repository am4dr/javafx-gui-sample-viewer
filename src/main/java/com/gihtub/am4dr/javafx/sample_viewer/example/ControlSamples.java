package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.sample.SimpleSample;
import com.gihtub.am4dr.javafx.sample_viewer.sample.UpdateAwareSample;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class ControlSamples extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {
        final SampleViewer viewer = new SampleViewer();
        addSamples(viewer);
        stage.setTitle("GUI control samples of SampleViewer");
        stage.setScene(new Scene(viewer.getView(), 600.0, 400.0));
        stage.show();
    }

    private void addSamples(SampleViewer viewer) {
        viewer.addSample(new SimpleSample<>("empty SampleViewer.View", new SampleViewer.View()));
        viewer.addSample(new UpdateAwareSample<>("SampleViewerSample by Class<T> class", SampleViewerSample.class, this::createWatcher));
        viewer.addSample(new UpdateAwareSample<>("SampleViewerSample by class name", SampleViewerSample.class.getName(), this::createWatcher));
    }
}
