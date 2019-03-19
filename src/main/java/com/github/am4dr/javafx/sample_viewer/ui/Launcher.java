package com.github.am4dr.javafx.sample_viewer.ui;

import javafx.application.Application;
import javafx.scene.Node;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.am4dr.javafx.sample_viewer.internal.UncheckedFunction.uncheckedFunction;
import static com.github.am4dr.javafx.sample_viewer.ui.NodeWrapper.Args.TARGET_PARAM_NAME;
import static com.github.am4dr.javafx.sample_viewer.ui.SampleApplicationSupport.Args.PATH_PARAM_NAME;

public final class Launcher {

    public static void main(String[] args) throws ClassNotFoundException {
        final var parsed = Args.parse(args);

        Application.launch(parsed.getLaunchTarget(), parsed.toOptions());
    }

    public static final class Args {

        private final String targetFQCN;
        private final List<String> paths;
        private final Class<?> targetClass;

        public Args(String targetFQCN, List<String> paths) throws ClassNotFoundException {
            this.targetFQCN = targetFQCN;
            this.paths = paths;
            targetClass = getTargetClass();
        }

        public String[] toLauncherArgs() {
            final ArrayList<String> args = new ArrayList<>();
            args.add(targetFQCN);
            args.addAll(paths);
            return args.toArray(new String[] {});
        }

        public static Args parse(String[] args) throws ClassNotFoundException {
            if (args.length < 1) {
                throw new IllegalArgumentException("target FQCN is not specified");
            }
            final List<String> paths = Arrays.stream(args, 1, args.length).collect(Collectors.toList());
            return new Args(args[0], paths);
        }

        public String[] toOptions() {
            final ArrayList<String> opts = new ArrayList<>();
            if (SampleApplicationSupport.class.isAssignableFrom(targetClass)) {
                opts.add("--"+PATH_PARAM_NAME+"="+String.join(File.pathSeparator, paths));
            }
            else if (Node.class.isAssignableFrom(targetClass)) {
                opts.add("--"+PATH_PARAM_NAME+"="+String.join(File.pathSeparator, paths));
                opts.add("--"+TARGET_PARAM_NAME+"="+targetFQCN);
            }
            return opts.toArray(new String[] {});
        }

        public Class<?> getTargetClass() throws ClassNotFoundException {
            final URL[] urls = paths.stream().map(Paths::get).map(uncheckedFunction(Path::toRealPath)).map(uncheckedFunction(p -> p.toUri().toURL())).toArray(URL[]::new);
            final Class<?> aClass = new URLClassLoader(urls).loadClass(targetFQCN);
            return aClass;
        }

        public Class<? extends Application> getLaunchTarget() {
            if (SampleApplicationSupport.class.isAssignableFrom(targetClass)) {
                return (Class<Application>) targetClass;
            }
            else if (Node.class.isAssignableFrom(targetClass)) {
                return NodeWrapper.class;
            }
            else {
                throw new IllegalArgumentException(String.format("%s is not an Application nor a Node", targetFQCN));
            }
        }
    }
}
