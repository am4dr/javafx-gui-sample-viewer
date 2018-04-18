package com.gihtub.am4dr.javafx.sample_viewer.ui;

import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static javafx.beans.binding.Bindings.createObjectBinding;

public final class StatusBorderedPane extends StackPane {

    private final Timeline borderFadeOut = new Timeline();
    private final double borderMaxOpacity = 0.75;
    private final DoubleProperty borderOpacity = new SimpleDoubleProperty(borderMaxOpacity);

    public StatusBorderedPane(UpdateAwareNode<?> updateAwareNode) {
        final var borderPane = new BorderPane();
        borderPane.centerProperty().bind(updateAwareNode);

        borderFadeOut.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(borderOpacity, borderMaxOpacity)),
                new KeyFrame(new Duration(750), new KeyValue(borderOpacity, borderMaxOpacity)),
                new KeyFrame(new Duration(1000), new KeyValue(borderOpacity, 0.0)));

        final var statusProperty = updateAwareNode.statusProperty();
        resetBorderColor(statusProperty.get());
        statusProperty.addListener((_o, old, status) -> resetBorderColor(status));

        final var borderLayer = new BorderLayer();
        borderLayer.setPickOnBounds(false);
        borderLayer.color.bind(createObjectBinding(() -> setOpacity(statusToColor(statusProperty.get()), borderOpacity.get()),
                borderOpacity, statusProperty));

        getChildren().addAll(borderPane, borderLayer);
    }

    private void resetBorderColor(UpdateAwareNode.STATUS status) {
        if (status == UpdateAwareNode.STATUS.OK) {
            borderFadeOut.playFromStart();
        }
        else {
            borderOpacity.set(borderMaxOpacity);
        }
    }
    private static Color statusToColor(UpdateAwareNode.STATUS status) {
        switch (status) {
            case OK:
                return Color.LIMEGREEN;
            case ERROR:
                return Color.DARKRED;
            case UPDATE_AWARE:
                return Color.DARKORANGE;
            case RELOADING:
                return Color.ORANGE;
            default:
                return Color.ANTIQUEWHITE;
        }
    }
    private static Color setOpacity(Color color, double opacity) {
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
    }


    public static class BorderLayer extends Pane {

        public ObjectProperty<Color> color = new SimpleObjectProperty<>();
        public DoubleProperty borderWidth = new SimpleDoubleProperty(10);

        private ObservableObjectValue<Border> border = new ObjectBinding<>() {
            { super.bind(color, borderWidth); }
            @Override
            protected Border computeValue() {
                return new Border(new BorderStroke(color.get(), BorderStrokeStyle.SOLID, null, new BorderWidths(borderWidth.get())));
            }
        };

        public BorderLayer() {
            borderProperty().bind(border);
        }
    }
}
