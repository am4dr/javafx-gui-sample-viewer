package sample.target;

import com.gihtub.am4dr.javafx.sample_viewer.ui.StatusBorderedPane;
import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import javafx.scene.Node;
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
        final UpdateAwareNode<Node> node = UpdateAwareNode.build(b -> b.type(ColorfulButtonCollection.class).classloader(this::createWatcher));
        return new BorderPane(new StatusBorderedPane(node));
    }
}
