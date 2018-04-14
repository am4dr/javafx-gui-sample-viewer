package com.gihtub.am4dr.javafx.sample_viewer.example;

import com.gihtub.am4dr.javafx.sample_viewer.SampleViewer;
import com.gihtub.am4dr.javafx.sample_viewer.sample.ClassBasedSample;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public final class SampleViewerSample extends VBox {

    private final SampleViewer viewer = new SampleViewer();

    public SampleViewerSample() {
        viewer.addSample(new ClassBasedSample<>("title 1", VBox.class, p -> {
            p.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.DASHED, new CornerRadii(4.0), new BorderWidths(2.0))));
        }));
        final SampleViewer.View view = viewer.getView();
        VBox.setVgrow(view, Priority.ALWAYS);
        getChildren().add(view);
    }
}
