package com.github.am4dr.javafx.sample_viewer.ui;

import com.github.am4dr.javafx.sample_viewer.UpdateAwareURLClassLoader;
import javafx.application.Application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SampleApplicationSupport extends Application {

    protected List<Path> paths;
    protected List<Path> loadOnlyPaths;

    @Override
    public void init() throws Exception {
        super.init();
        paths = parseToPathList(getParameters().getNamed().get("path"));
        loadOnlyPaths = parseToPathList(getParameters().getNamed().get("load-only-path"));
    }
    protected UpdateAwareURLClassLoader createWatcher() {
        return new UpdateAwareURLClassLoader(paths, loadOnlyPaths);
    }

    private static List<Path> parseToPathList(String paths) {
        if (paths == null) return List.of();
        return Arrays.stream(paths.split(File.pathSeparator))
                .map(Paths::get).distinct().collect(Collectors.toList());
    }
}
