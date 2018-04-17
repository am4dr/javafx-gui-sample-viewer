package com.gihtub.am4dr.javafx.sample_viewer.ui;

import javafx.application.Application;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.gihtub.am4dr.javafx.sample_viewer.util.UncheckedFunction.uncheckedFunction;

public final class Launcher {

    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 1) {
            throw new IllegalArgumentException("target FQCN is not specified");
        }
        final String targetFQCN = args[0];
        final List<String> paths = Arrays.stream(args, 1, args.length).collect(Collectors.toList());
        final URL[] urls = paths.stream().map(Paths::get).map(uncheckedFunction(Path::toRealPath)).map(uncheckedFunction(p -> p.toUri().toURL())).toArray(URL[]::new);
        final URLClassLoader classLoader = new URLClassLoader(urls);
        final Class<?> aClass = classLoader.loadClass(targetFQCN);
        Application.launch((Class<? extends Application>) aClass, "--path="+String.join(File.pathSeparator, paths));
    }
}
