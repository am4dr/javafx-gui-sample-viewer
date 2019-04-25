package sample.target;

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
        return new BorderPane(new StatusBorderedPane(createNodeBinding(ColorfulButtonCollection.class)));
    }
}
