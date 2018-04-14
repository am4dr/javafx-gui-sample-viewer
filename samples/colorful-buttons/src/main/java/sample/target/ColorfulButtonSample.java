package sample.target;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
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

public final class ColorfulButtonSample extends VBox {

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

    private final ObservableList<Button> buttons = colors.stream().map(c -> {
        final var button = new Button();
        button.setBackground(new Background(new BackgroundFill(c, new CornerRadii(2.0), null)));
        button.prefWidthProperty().bind(button.heightProperty());
        final var tip = new Tooltip(c.toString());
        tip.setShowDelay(new Duration(500));
        button.setTooltip(tip);
        button.effectProperty().bind(new When(button.hoverProperty()).then(new DropShadow()).otherwise((DropShadow)null));
        return button;
    }).collect(Collectors.toCollection(FXCollections::observableArrayList));

    public ColorfulButtonSample() {
        setPadding(new Insets(10));
        final var tilePane = new TilePane(5.0, 7.0);
        tilePane.prefWidthProperty().bind(widthProperty().subtract(40));
        tilePane.setPadding(new Insets(10));
        tilePane.setAlignment(Pos.CENTER);
        tilePane.setBackground(new Background(new BackgroundFill(Color.rgb(20, 20, 20), null, null)));
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
