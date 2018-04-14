package com.gihtub.am4dr.javafx.sample_viewer;

import javafx.application.Application;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedFunction.uncheckedFunction;

public class Launcher {

    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 1) {
            throw new IllegalArgumentException("target FQCN is not specified");
        }
        final String targetFQCN = args[0];
        final List<String> paths = Arrays.stream(args, 1, args.length).collect(Collectors.toList());
        final ClassPathWatcher classLoader = new ClassPathWatcher(paths.stream().map(Paths::get).map(uncheckedFunction(Path::toRealPath)).collect(Collectors.toList()));
        final Class<?> aClass = classLoader.loadClass(targetFQCN);
        Application.launch((Class<? extends Application>) aClass, "--path="+String.join(File.pathSeparator, paths));
    }
}
