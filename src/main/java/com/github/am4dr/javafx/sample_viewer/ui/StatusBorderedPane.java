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
    private final DoubleProperty borderOpacity = new SimpleDoubleProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>();
    private final BorderLayer borderLayer = new BorderLayer();
    private final BorderPane basePane = new BorderPane();

    {
        borderFadeOut.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(borderOpacity, borderMaxOpacity)),
                new KeyFrame(new Duration(750), new KeyValue(borderOpacity, borderMaxOpacity)),
                new KeyFrame(new Duration(1000), new KeyValue(borderOpacity, 0.0)));

        status.addListener((observableValue, oldValue, newValue) -> {
            if (newValue == Status.OK) {
                borderFadeOut.playFromStart();
            }
            else {
                borderOpacity.set(borderMaxOpacity);
            }
        });
        status.set(Status.PROCESSING);

        borderLayer.setPickOnBounds(false);
        borderLayer.color.bind(createObjectBinding(() -> status.get().getDefaultColor(), status));
        borderLayer.opacityProperty().bind(borderOpacity);
        getChildren().addAll(basePane, borderLayer);
    }

    @Deprecated(forRemoval = true, since = "0.5")
    public StatusBorderedPane(UpdateAwareNode<?> updateAwareNode) {
        basePane.centerProperty().bind(updateAwareNode);
        final var statusProperty = updateAwareNode.statusProperty();
        status.bind(createObjectBinding(() -> {
            switch(statusProperty.get()) {
                case OK:
                    return Status.OK;
                case RELOADING:
                    return Status.PROCESSING;
                case UPDATE_DETECTED:
                    return Status.WARNING;
                case ERROR:
                default:
                    return Status.ERROR;
            }
        }, statusProperty));
    }

    public StatusBorderedPane(NodeLatestInstanceBinding nodeBinding) {
        basePane.centerProperty().bind(nodeBinding);
        final var statusProperty = nodeBinding.statusProperty();
        status.bind(createObjectBinding(() -> {
            switch(statusProperty.get()) {
                case OK:
                    return Status.OK;
                case RELOADING:
                    return Status.PROCESSING;
                case UPDATE_DETECTED:
                    return Status.WARNING;
                case ERROR:
                default:
                    return Status.ERROR;
            }
        }, statusProperty));
    }


    public enum Status {
        OK(Color.LIMEGREEN), ERROR(Color.DARKRED), WARNING(Color.DARKORANGE), PROCESSING(Color.ORANGE);
        private final Color defaultColor;

        Status(Color color) {
            defaultColor = color;
        }

        public Color getDefaultColor() {
            return defaultColor;
        }
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
