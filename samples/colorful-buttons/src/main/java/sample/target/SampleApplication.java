package sample.target;

import com.gihtub.am4dr.javafx.sample_viewer.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SampleApplication extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {
        final var viewer = new SampleViewer();
        addSamples(viewer);
        stage.setTitle("GUI control samples of SampleViewer");
        stage.setScene(new Scene(viewer.getView(), 600.0, 400.0));
        stage.show();
    }

    private void addSamples(SampleViewer viewer) {
        viewer.addSample("colorful buttons!", new UpdateAwareNode<>(this::createWatcher, ColorfulButtonSample.class.getName()));
        viewer.addSample("colorful buttons!", new UpdateAwareNode<>(this::createWatcher, ColorfulButtonSample.class));
    }
}
