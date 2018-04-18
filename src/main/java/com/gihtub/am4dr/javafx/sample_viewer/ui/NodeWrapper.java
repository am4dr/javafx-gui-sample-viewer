package com.gihtub.am4dr.javafx.sample_viewer.ui;

import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class NodeWrapper extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {
        final String target = getParameters().getNamed().get("target");
        final StatusBorderedPane pane = new StatusBorderedPane(UpdateAwareNode.build(b -> b.name(target).classloader(this::createWatcher)));
        stage.setTitle(target);
        stage.setScene(new Scene(pane, 600, 400));
        stage.show();
    }
}
