package sample.target;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.scene.paint.Color.*;

public final class ColorfulButtonCollection extends VBox {

    private static final List<Color> colors = List.of(
            ALICEBLUE, ANTIQUEWHITE, AQUA, AQUAMARINE, AZURE, BEIGE, BISQUE, BLACK,
            BLANCHEDALMOND, BLUE, BLUEVIOLET, BROWN, BURLYWOOD, CADETBLUE, CHARTREUSE,
            CHOCOLATE, CORAL, CORNFLOWERBLUE, CORNSILK, CRIMSON, CYAN, DARKBLUE, DARKCYAN,
            DARKGOLDENROD, DARKGRAY, DARKGREEN, DARKGREY, DARKKHAKI, DARKMAGENTA,
            DARKOLIVEGREEN, DARKORANGE, DARKORCHID, DARKRED, DARKSALMON, DARKSEAGREEN,
            DARKSLATEBLUE, DARKSLATEGRAY, DARKSLATEGREY, DARKTURQUOISE, DARKVIOLET,
            DEEPPINK, DEEPSKYBLUE, DIMGRAY, DIMGREY, DODGERBLUE, FIREBRICK, FLORALWHITE,
            FORESTGREEN, FUCHSIA, GAINSBORO, GHOSTWHITE, GOLD, GOLDENROD, GRAY, GREEN,
            GREENYELLOW, GREY, HONEYDEW, HOTPINK, INDIANRED, INDIGO, IVORY, KHAKI,
            LAVENDER, LAVENDERBLUSH, LAWNGREEN, LEMONCHIFFON, LIGHTBLUE, LIGHTCORAL,
            LIGHTCYAN, LIGHTGOLDENRODYELLOW, LIGHTGRAY, LIGHTGREEN, LIGHTGREY, LIGHTPINK,
            LIGHTSALMON, LIGHTSEAGREEN, LIGHTSKYBLUE, LIGHTSLATEGRAY, LIGHTSLATEGREY,
            LIGHTSTEELBLUE, LIGHTYELLOW, LIME, LIMEGREEN, LINEN, MAGENTA, MAROON,
            MEDIUMAQUAMARINE, MEDIUMBLUE, MEDIUMORCHID, MEDIUMPURPLE, MEDIUMSEAGREEN,
            MEDIUMSLATEBLUE, MEDIUMSPRINGGREEN, MEDIUMTURQUOISE, MEDIUMVIOLETRED,
            MIDNIGHTBLUE, MINTCREAM, MISTYROSE, MOCCASIN, NAVAJOWHITE, NAVY, OLDLACE,
            OLIVE, OLIVEDRAB, ORANGE, ORANGERED, ORCHID, PALEGOLDENROD, PALEGREEN,
            PALETURQUOISE, PALEVIOLETRED, PAPAYAWHIP, PEACHPUFF, PERU, PINK, PLUM,
            POWDERBLUE, PURPLE, RED, ROSYBROWN, ROYALBLUE, SADDLEBROWN, SALMON, SANDYBROWN,
            SEAGREEN, SEASHELL, SIENNA, SILVER, SKYBLUE, SLATEBLUE, SLATEGRAY, SLATEGREY,
            SNOW, SPRINGGREEN, STEELBLUE, TAN, TEAL, THISTLE, TOMATO, TRANSPARENT,
            TURQUOISE, VIOLET, WHEAT, WHITE, WHITESMOKE, YELLOW, YELLOWGREEN);

    public final DoubleProperty buttonSize = new SimpleDoubleProperty(24);

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.rgb(20, 20, 20));
    private final Timeline timeline = new Timeline();
    private void changeBackgroundColor(Color to) {
        timeline.stop();
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(new KeyFrame(new Duration(1000), new KeyValue(backgroundColor, to)));
        timeline.playFromStart();
    }
    private final ObservableObjectValue<Background> background = Bindings.createObjectBinding(() ->
            new Background(new BackgroundFill(backgroundColor.get(), null, null)), backgroundColor);

    private final ObservableList<Button> buttons = colors.stream().map(c -> {
        final var button = new Button();
        button.setBackground(new Background(new BackgroundFill(c, new CornerRadii(2.0), null)));
        button.prefWidthProperty().bind(buttonSize);
        button.prefHeightProperty().bind(buttonSize);
        button.setTooltip(new Tooltip(c.toString()));
        button.getTooltip().setShowDelay(new Duration(500));
        button.effectProperty().bind(new When(button.hoverProperty()).then(new DropShadow(4, Color.WHITE)).otherwise((DropShadow)null));
        button.setOnAction(e -> changeBackgroundColor(c));
        return button;
    }).collect(Collectors.toCollection(FXCollections::observableArrayList));

    public ColorfulButtonCollection() {
        setPadding(new Insets(10));
        final var tilePane = new TilePane(5.0, 7.0);
        tilePane.prefWidthProperty().bind(widthProperty().subtract(40));
        tilePane.setPadding(new Insets(10));
        tilePane.setAlignment(Pos.CENTER);
        tilePane.backgroundProperty().bind(background);
        VBox.setVgrow(tilePane, Priority.ALWAYS);
        Bindings.bindContent(tilePane.getChildren(), buttons);

        final var sort = new FlowPane(new Label("sort: "),
                new Button("reverse") {{
                    setOnAction(e -> FXCollections.reverse(buttons));
                }},
                new Button("by Red") {{
                    setOnAction(e -> FXCollections.sort(buttons, Comparator.comparing(it -> getColor(it).getRed(), Comparator.reverseOrder())));
                }},
                new Button("by Green") {{
                    setOnAction(e -> FXCollections.sort(buttons, Comparator.comparing(it -> getColor(it).getGreen(), Comparator.reverseOrder())));
                }},
                new Button("by Blue") {{
                    setOnAction(e -> FXCollections.sort(buttons, Comparator.comparing(it -> getColor(it).getBlue(), Comparator.reverseOrder())));
                }},
                new Button("by Gray") {{
                    setOnAction(e -> FXCollections.sort(buttons, Comparator.comparing(it -> getColor(it).grayscale().getGreen(), Comparator.reverseOrder())));
                }},
                new Button("by Hue") {{
                    setOnAction(e -> FXCollections.sort(buttons, Comparator.comparing(it -> getColor(it).getHue(), Comparator.reverseOrder())));
                }});
        getChildren().addAll(sort, tilePane);
    }
    private static Color getColor(Button button) {
        return (Color)button.getBackground().getFills().get(0).getFill();
    }
}
