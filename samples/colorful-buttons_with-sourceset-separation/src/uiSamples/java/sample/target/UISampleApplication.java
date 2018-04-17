package sample.target;

import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport;
import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
        final UpdateAwareNode<Node> node = UpdateAwareNode.build(b -> b.type(ColorfulButtonCollection.class).classloader(this::createWatcher));
        borderPane.centerProperty().bind(node);
        borderPane.setTop(new StatusBar(node));
        return borderPane;
    }

    private static class StatusBar extends Pane {

        public StatusBar(UpdateAwareNode<?> node) {
            setPrefHeight(10.0);
            setMaxHeight(Region.USE_PREF_SIZE);
            setMinHeight(Region.USE_PREF_SIZE);
            final var statusProperty = node.statusProperty();
            backgroundProperty().bind(Bindings.createObjectBinding(() -> colorToBackground(statusToColor(statusProperty.get())), statusProperty));
        }
        private Background colorToBackground(Color color) {
            return new Background(new BackgroundFill(color, null, null));
        }
        private Color statusToColor(UpdateAwareNode.STATUS status) {
            switch (status) {
                case OK:
                    return Color.LIMEGREEN;
                case ERROR:
                    return Color.DARKRED;
                case UPDATE_AWARE:
                    return Color.DARKORANGE;
                case RELOADING:
                    return Color.ORANGE;
            }
            return  Color.ANTIQUEWHITE;
        }
    }
}
