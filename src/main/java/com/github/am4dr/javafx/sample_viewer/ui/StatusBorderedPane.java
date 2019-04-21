package com.github.am4dr.javafx.sample_viewer.ui;

import com.github.am4dr.javafx.sample_viewer.NodeLatestInstanceBinding;
import com.github.am4dr.javafx.sample_viewer.UpdateAwareNode;
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
    private final ObjectProperty<Color> borderColor = new SimpleObjectProperty<>();
    private BorderLayer borderLayer = new BorderLayer();
    private BorderPane basePane = new BorderPane();

    {
        borderFadeOut.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(borderOpacity, borderMaxOpacity)),
                new KeyFrame(new Duration(750), new KeyValue(borderOpacity, borderMaxOpacity)),
                new KeyFrame(new Duration(1000), new KeyValue(borderOpacity, 0.0)));

        borderLayer.setPickOnBounds(false);
        borderLayer.color.bind(borderColor);
        getChildren().addAll(basePane, borderLayer);
    }

    @Deprecated(forRemoval = true, since = "0.5")
    public StatusBorderedPane(UpdateAwareNode<?> updateAwareNode) {
        basePane.centerProperty().bind(updateAwareNode);

        final var statusProperty = updateAwareNode.statusProperty();
        resetBorderColor(statusProperty.get());
        borderColor.bind(createObjectBinding(() -> withOpacity(statusToColor(statusProperty.get()), borderOpacity.get()), borderOpacity));
        statusProperty.addListener((_o, old, status) -> {
            resetBorderColor(status);
            borderColor.bind(createObjectBinding(() -> withOpacity(statusToColor(status), borderOpacity.get()), borderOpacity));
        });
    }

    public StatusBorderedPane(NodeLatestInstanceBinding nodeBinding) {
        basePane.centerProperty().bind(nodeBinding);
        final var statusProperty = nodeBinding.statusProperty();
        resetBorderColor(statusProperty.get());
        borderColor.bind(createObjectBinding(() -> withOpacity(statusToColor(statusProperty.get()), borderOpacity.get()), borderOpacity));
        statusProperty.addListener((_o, old, status) -> {
            resetBorderColor(status);
            borderColor.bind(createObjectBinding(() -> withOpacity(statusToColor(status), borderOpacity.get()), borderOpacity));
        });
    }

    private void resetBorderColor(NodeLatestInstanceBinding.STATUS status) {
        if (status == NodeLatestInstanceBinding.STATUS.OK) {
            borderFadeOut.playFromStart();
        }
        else {
            borderOpacity.set(borderMaxOpacity);
        }
    }
    private Color statusToColor(NodeLatestInstanceBinding.STATUS status) {
        switch (status) {
            case OK:
                return Color.LIMEGREEN;
            case ERROR:
                return Color.DARKRED;
            case UPDATE_DETECTED:
                return Color.DARKORANGE;
            case RELOADING:
                return Color.ORANGE;
            default:
                return Color.ANTIQUEWHITE;
        }
    }

    @Deprecated(forRemoval = true, since = "0.5")
    private void resetBorderColor(UpdateAwareNode.STATUS status) {
        if (status == UpdateAwareNode.STATUS.OK) {
            borderFadeOut.playFromStart();
        }
        else {
            borderOpacity.set(borderMaxOpacity);
        }
    }
    @Deprecated(forRemoval = true, since = "0.5")
    private static Color statusToColor(UpdateAwareNode.STATUS status) {
        switch (status) {
            case OK:
                return Color.LIMEGREEN;
            case ERROR:
                return Color.DARKRED;
            case UPDATE_DETECTED:
                return Color.DARKORANGE;
            case RELOADING:
                return Color.ORANGE;
            default:
                return Color.ANTIQUEWHITE;
        }
    }
    private static Color withOpacity(Color color, double opacity) {
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
