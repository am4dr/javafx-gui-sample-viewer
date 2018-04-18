package com.gihtub.am4dr.javafx.sample_viewer.ui;

import com.gihtub.am4dr.javafx.sample_viewer.UpdateAwareNode;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.gihtub.am4dr.javafx.sample_viewer.internal.UncheckedFunction.uncheckedFunction;

public final class Launcher {

    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length < 1) {
            throw new IllegalArgumentException("target FQCN is not specified");
        }
        final String targetFQCN = args[0];
        final List<String> paths = Arrays.stream(args, 1, args.length).collect(Collectors.toList());
        final URL[] urls = paths.stream().map(Paths::get).map(uncheckedFunction(Path::toRealPath)).map(uncheckedFunction(p -> p.toUri().toURL())).toArray(URL[]::new);
        final Class<?> aClass = new URLClassLoader(urls).loadClass(targetFQCN);
        final String optPath = "--path=" + String.join(File.pathSeparator, paths);
        if (Application.class.isAssignableFrom(aClass)) {
            Application.launch((Class<? extends Application>) aClass, optPath);
        }
        else if (Node.class.isAssignableFrom(aClass)) {
            Application.launch(NodeWrapper.class, "--target="+targetFQCN, optPath);
        }
        else {
            throw new IllegalArgumentException(String.format("%s is not an Application nor a Node", targetFQCN));
        }
    }
}
