package com.gihtub.am4dr.javafx.sample_viewer;

import javafx.application.Application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SampleApplicationSupport extends Application {

    protected List<Path> paths;
    @Override
    public void init() throws Exception {
        super.init();
        paths = Arrays.stream(getParameters().getNamed().get("path").split(File.pathSeparator))
                .map(Paths::get).distinct().collect(Collectors.toList());
    }
    protected ClassPathWatcher createWatcher() {
        return new ClassPathWatcher(paths);
    }
}
