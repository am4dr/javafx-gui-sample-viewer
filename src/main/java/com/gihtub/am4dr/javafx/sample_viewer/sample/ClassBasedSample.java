package com.gihtub.am4dr.javafx.sample_viewer.sample;

import com.gihtub.am4dr.javafx.sample_viewer.Sample;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public final class ClassBasedSample<R extends Node> extends Sample<R> {

    public final Class<R> viewClass;

    public ClassBasedSample(String title, Class<R> viewClass) {
        super(title);
        this.viewClass = viewClass;
    }
    public ClassBasedSample(String title, Class<R> viewClass, Consumer<? super R> initializer) {
        super(title, initializer);
        this.viewClass = viewClass;
    }

    @Override
    public ReadOnlyObjectProperty<R> nodeProperty() {
        if (node.get() == null) {
            try {
                var newNode = viewClass.getDeclaredConstructor().newInstance();
                initializer.accept(newNode);
                node.set(newNode);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return node.getReadOnlyProperty();
    }
}
