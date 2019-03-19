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

        parsed.launch();
    }


    public static final class Args {

        private final String targetFQCN;
        private final List<String> paths;

        public Args(String targetFQCN, List<String> paths) {
            this.targetFQCN = targetFQCN;
            this.paths = paths;
        }
        public Args(String targetFQCN, String... paths) {
            this(targetFQCN, List.of(paths));
        }

        public String[] toLauncherArgs() {
            final ArrayList<String> args = new ArrayList<>();
            args.add(targetFQCN);
            args.addAll(paths);
            return args.toArray(new String[] {});
        }

        public static Args parse(String[] args) {
            if (args.length < 1) {
                throw new IllegalArgumentException("target FQCN is not specified");
            }
            final List<String> paths = Arrays.stream(args, 1, args.length).collect(Collectors.toList());
            return new Args(args[0], paths);
        }


        public void launch() throws ClassNotFoundException {
            final var targetClass = getTargetClass();
            Application.launch(getLaunchTarget(targetClass), toOptions(targetClass));
        }

        private Class<?> getTargetClass() throws ClassNotFoundException {
            final URL[] urls = paths.stream().map(Paths::get).map(uncheckedFunction(Path::toRealPath)).map(uncheckedFunction(p -> p.toUri().toURL())).toArray(URL[]::new);
            return new URLClassLoader(urls).loadClass(targetFQCN);
        }

        private String[] toOptions(Class<?> targetClass) {
            final var opts = new ArrayList<String>();
            if (SampleApplicationSupport.class.isAssignableFrom(targetClass)) {
                opts.add("--"+PATH_PARAM_NAME+"="+String.join(File.pathSeparator, paths));
            }
            else if (Node.class.isAssignableFrom(targetClass)) {
                opts.add("--"+PATH_PARAM_NAME+"="+String.join(File.pathSeparator, paths));
                opts.add("--"+TARGET_PARAM_NAME+"="+targetFQCN);
            }
            return opts.toArray(new String[] {});
        }

        private Class<? extends Application> getLaunchTarget(Class<?> targetClass) {
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
