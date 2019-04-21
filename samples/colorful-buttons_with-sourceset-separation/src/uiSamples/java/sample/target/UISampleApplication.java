package sample.target;

import com.github.am4dr.javafx.sample_viewer.NodeLatestInstanceBinding;
import com.github.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import com.github.am4dr.javafx.sample_viewer.ui.StatusBorderedPane;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class UISampleApplication extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {
        stage.setTitle("GUI control samples of SampleViewer");
        stage.setScene(new Scene(createView(), 600.0, 400.0));
        stage.show();
    }
    private BorderPane createView() {
        final var node = NodeLatestInstanceBinding.build(b -> b.type(ColorfulButtonCollection.class).classloader(this::createWatcher));
        return new BorderPane(new StatusBorderedPane(node));
    }
}
