package com.github.am4dr.javafx.sample_viewer;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Flow;

public interface ReportingClassLoader {

    Flow.Publisher<Path> getLoadedPathPublisher();
    Optional<Class<?>> load(String fqcn);
    void shutdown();
}
