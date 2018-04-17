package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleCollection;
import com.gihtub.am4dr.javafx.sample_viewer.ui.SampleCollectionViewer;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public final class SampleViewerSample extends VBox {

    public SampleViewerSample() {
        final SampleCollection samples = new SampleCollection();
        samples.addSample("title 1", new VBox() {{
            setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, new CornerRadii(4.0), new BorderWidths(2.0))));
        }});
        final var viewer = new SampleCollectionViewer(samples);
        final SampleCollectionViewer.View view = viewer.getView();
        VBox.setVgrow(view, Priority.ALWAYS);
        getChildren().add(view);
    }
}
