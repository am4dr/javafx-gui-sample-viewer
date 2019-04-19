package com.github.am4dr.javafx.sample_viewer;


import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 *
 * TODO PathWatchEventPublisherから通知を受けてリロードを試みるサブスクライバの実装
 */
public final class LatestInstanceProvider {

    private final String fqcn;
    private final Supplier<ReportingClassLoader> classLoaderSupplier;
    private final PathWatcherImpl pathWatcher;
    private final PathWatchEventPublisher watchEventPublisher;
    private final ExecutorService workerExecutorService;
    private final ExecutorService executorService;

    // TODO PathWatcherとPathWatchEventPublisherはファサードにまとめていいかも
    public LatestInstanceProvider(String fqcn,
                                  Supplier<ReportingClassLoader> classLoaderSupplier,
                                  PathWatcherImpl pathWatcher,
                                  PathWatchEventPublisher watchEventPublisher,
                                  ExecutorService workerExecutorService,
                                  ExecutorService publisherExecutorService) {
        this.fqcn = fqcn;
        this.classLoaderSupplier = classLoaderSupplier;
        this.pathWatcher = pathWatcher;
        this.watchEventPublisher = watchEventPublisher;
        this.workerExecutorService = workerExecutorService;
        this.executorService = publisherExecutorService;
        updateStatus(Status.INITIALIZED);
    }
    public LatestInstanceProvider(String fqcn,
                                  Supplier<ReportingClassLoader> classLoaderSupplier,
                                  PathWatcherImpl pathWatcher,
                                  PathWatchEventPublisher watchEventPublisher,
                                  ExecutorService workerExecutorService) {
        this(fqcn, classLoaderSupplier, pathWatcher, watchEventPublisher, workerExecutorService, workerExecutorService);
    }

    public String getFQCN() {
        return fqcn;
    }


    private void load() {
        final var classLoader = classLoaderSupplier.get();
        // TODO 読み込んだクラスのPathをwatcherに登録するサブスクライバを実装する
        classLoader.subscribe(new SimpleSubscriber<>() {
            @Override
            public void onNext(Path item) {

            }
        });

        updateStatus(Status.LOADING);
        final Optional<Class<?>> clazz = classLoader.load(fqcn);
        clazz.map(c -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            } catch (Throwable e) {
                return null;
            }
        }).ifPresentOrElse(instance -> {
            updateInstance(instance, classLoader);
            updateStatus(Status.LOAD_SUCCEEDED);
        }, () -> {
            classLoader.shutdown();
            updateStatus(Status.LOAD_FAILED);
        });
    }


    // TODO 最新のインスタンスとそのローダーを保持するものとしてinternalに抽出(LatestInstanceHolder?)
    private final AtomicReference<LoadedInstance> atomicInstance = new AtomicReference<>();
    public Optional<Object> getInstance() {
        return Optional.ofNullable(atomicInstance.get()).map(it -> it.instance);
    }
    private void updateInstance(Object instance, ReportingClassLoader reportingClassLoader) {
        final var oldInstance = atomicInstance.getAndSet(new LoadedInstance(instance, reportingClassLoader));
        oldInstance.dispose();
    }
    private static class LoadedInstance {
        private final Object instance;
        private final ReportingClassLoader classLoader;

        LoadedInstance(Object instance, ReportingClassLoader classLoader) {
            this.instance = instance;
            this.classLoader = classLoader;
        }

        void dispose() {
            classLoader.shutdown();
        }
    }


    private void updateStatus(Status status) {
    }


    public enum Status {
        INITIALIZED, LOADING, LOAD_SUCCEEDED, CHANGE_DETECTED, LOAD_FAILED
    }
}
