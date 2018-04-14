package sample.target;

import com.gihtub.am4dr.javafx.sample_viewer.Launcher;
import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.sample.NameBasedSample;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SampleApplication extends Launcher {

    @Override
    public void start(Stage stage) {
        stage.setTitle("GUI control samples of SampleViewer");
        final var viewer = new SampleViewer();
        addSamples(viewer);
        final var scene = new Scene(viewer.getView(), 600.0, 400.0);
        stage.setScene(scene);
        stage.show();
    }

    private void addSamples(SampleViewer viewer) {
        viewer.addSample(new NameBasedSample<>("colorful buttons!", ColorfulButtonSample.class.getName(), ColorfulButtonSample.class, this::createWatcher));
    }
}
