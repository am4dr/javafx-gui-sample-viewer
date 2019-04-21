package com.github.am4dr.javafx.sample_viewer;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public final class PathWatchEvent {
    public final Path path;
    public final WatchEvent.Kind<?> kind;

    public PathWatchEvent(WatchEvent<?> event, Path watchable) {
        path = watchable.resolve((Path)event.context());
        kind = event.kind();
    }
}
