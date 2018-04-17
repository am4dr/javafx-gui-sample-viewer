package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
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
        viewer.addSample("empty SampleViewer.View", new SampleViewer.View());
        viewer.addSample("SampleViewerSample by Class<T> class", new UpdateAwareNode<>(this::createWatcher, SampleViewerSample.class));
        viewer.addSample("SampleViewerSample by class name", new UpdateAwareNode<>(this::createWatcher, SampleViewerSample.class.getName()));
    }
}
