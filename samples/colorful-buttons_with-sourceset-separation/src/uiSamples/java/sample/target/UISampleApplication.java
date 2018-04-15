package sample.target;

import com.gihtub.am4dr.javafx.sample_viewer.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.sample.UpdateAwareSample;
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
        final BorderPane borderPane = new BorderPane();
        borderPane.centerProperty().bind(new UpdateAwareSample<>(this::createWatcher, ColorfulButtonCollection.class).nodeProperty());
        return borderPane;
    }
}
