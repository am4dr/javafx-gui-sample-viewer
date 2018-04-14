package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.ClassPathWatcher;
import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.sample.ClassBasedSample;
import com.gihtub.am4dr.javafx.sample_viewer.sample.NameBasedSample;
import com.gihtub.am4dr.javafx.sample_viewer.sample.SimpleSample;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ControlSamples extends Application {

    private SampleViewer viewer;
    private List<Path> paths;

    @Override
    public void init() throws Exception {
        super.init();
        paths = Arrays.stream(getParameters().getNamed().get("path").split(File.pathSeparator))
                .map(Paths::get).distinct().collect(Collectors.toList());
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("GUI control samples of SampleViewer");
        viewer = new SampleViewer();
        addSamples(viewer);
        final var scene = new Scene(viewer.getView(), 600.0, 400.0);
        stage.setScene(scene);
        stage.show();
    }

    private ClassPathWatcher createWatcher() {
        return new ClassPathWatcher(paths);
    }

    private void addSamples(SampleViewer viewer) {
        viewer.addSample(new ClassBasedSample<>("empty Pane()", Pane.class));
        viewer.addSample(new ClassBasedSample<>("empty SampleViewer.View", SampleViewer.View.class));
        viewer.addSample(new ClassBasedSample<>("SampleViewerSample by Class<T> class", SampleViewerSample.class));
        viewer.addSample(new NameBasedSample<>("SampleViewerSample by class name", SampleViewerSample.class.getName(), Node.class, ClassPathWatcher::new));
        viewer.addSample(new SimpleSample<>("SampleViewerSample by class instance", new SampleViewerSample()));
        viewer.addSample(new NameBasedSample<>("color samples", "sample.target.ColorfulButtonSample", Node.class, this::createWatcher));
    }
}
