package com.github.am4dr.javafx.sample_viewer.ui;

import com.github.am4dr.javafx.sample_viewer.NodeLatestInstanceBinding;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static com.github.am4dr.javafx.sample_viewer.ui.NodeWrapper.Args.TARGET_PARAM_NAME;

public final class NodeWrapper extends SampleApplicationSupport {

    @Override
    public void start(Stage stage) {
        final String target = getParameters().getNamed().get(TARGET_PARAM_NAME);
        final StatusBorderedPane pane = new StatusBorderedPane(NodeLatestInstanceBinding.build(b -> b.name(target).classloader(this::createWatcher)));
        stage.setTitle(target);
        stage.setScene(new Scene(pane, 600, 400));
        stage.show();
    }


    public static final class Args {
        public static final String TARGET_PARAM_NAME = "target-fqcn";
    }
}
