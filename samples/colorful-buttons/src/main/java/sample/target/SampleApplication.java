package sample.target;

import com.github.am4dr.javafx.sample_viewer.NodeLatestInstanceBinding;
import com.github.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import com.github.am4dr.javafx.sample_viewer.ui.SampleCollection;
import com.github.am4dr.javafx.sample_viewer.ui.SampleCollectionViewer;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SampleApplication extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {

        final var viewer = new SampleCollectionViewer(createSamples());
        stage.setTitle("GUI control samples of SampleViewer");
        stage.setScene(new Scene(viewer.getView(), 600.0, 400.0));
        stage.show();
    }

    private SampleCollection createSamples() {
        final SampleCollection samples = new SampleCollection();
        samples.addSample("colorful buttons!", new NodeLatestInstanceBinding(this::createClassLoader, ColorfulButtonSample.class.getName()));
        samples.addSample("colorful buttons!", new NodeLatestInstanceBinding(this::createClassLoader, ColorfulButtonSample.class));
        samples.addSample("colorful buttons!", createNodeBinding(ColorfulButtonSample.class.getName()));
        samples.addSample("colorful buttons!", createNodeBinding(ColorfulButtonSample.class));
        return samples;
    }
}
