package com.github.am4dr.javafx.sample_viewer;

import java.util.Map;

public interface RestorableNode {

    void restore(Map<String, Object> states);
}
