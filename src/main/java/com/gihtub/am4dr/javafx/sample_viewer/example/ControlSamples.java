package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.ClassPathWatcher;
import com.gihtub.am4dr.javafx.sample_viewer.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.sample.ClassBasedSample;
import com.gihtub.am4dr.javafx.sample_viewer.sample.NameBasedSample;
import com.gihtub.am4dr.javafx.sample_viewer.sample.SimpleSample;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
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
        viewer.addSample(new ClassBasedSample<>("empty Pane()", Pane.class));
        viewer.addSample(new ClassBasedSample<>("empty SampleViewer.View", SampleViewer.View.class));
        viewer.addSample(new ClassBasedSample<>("SampleViewerSample by Class<T> class", SampleViewerSample.class));
        viewer.addSample(new NameBasedSample<>("SampleViewerSample by class name", SampleViewerSample.class.getName(), Node.class, ClassPathWatcher::new));
        viewer.addSample(new SimpleSample<>("SampleViewerSample by class instance", new SampleViewerSample()));
    }
}
