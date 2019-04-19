package com.github.am4dr.javafx.sample_viewer;


import com.github.am4dr.javafx.sample_viewer.internal.DaemonThreadFactory;
import com.github.am4dr.javafx.sample_viewer.internal.SimpleSubscriber;
import com.github.am4dr.javafx.sample_viewer.internal.WaitLastProcessor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Objects.requireNonNull;

/**
 *
 * TODO PathWatchEventPublisherから通知を受けてリロードを試みるサブスクライバの実装
 */
public final class LatestInstanceProvider {

    private final String fqcn;
    private final Supplier<ReportingClassLoader> classLoaderSupplier;
    private final PathWatcherImpl pathWatcher;
    private final ExecutorService workerExecutorService;

    private List<Path> requiredClasses = Collections.synchronizedList(new ArrayList<>());

    private final ScheduledExecutorService waitLastProcessorExecutor = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.INSTANCE);
    private final WaitLastProcessor<List<PathWatcher.PathWatchEvent>> pathWatchEventWaitLastProcessor =
            new WaitLastProcessor<>(waitLastProcessorExecutor, 100, TimeUnit.MILLISECONDS);

    // TODO PathWatcherとPathWatchEventPublisherはファサードにまとめていいかも
    public LatestInstanceProvider(String fqcn,
                                  Supplier<ReportingClassLoader> classLoaderSupplier,
                                  PathWatcherImpl pathWatcher,
                                  PathWatchEventPublisher watchEventPublisher,
                                  ExecutorService workerExecutorService) {
        this.fqcn = fqcn;
        this.classLoaderSupplier = classLoaderSupplier;
        this.pathWatcher = pathWatcher;
        this.workerExecutorService = workerExecutorService;
        updateStatus(Status.INITIALIZED);

        watchEventPublisher.subscribe(pathWatchEventWaitLastProcessor);
        pathWatchEventWaitLastProcessor.subscribe(new SimpleSubscriber<>() {
            @Override
            public void process(List<PathWatcher.PathWatchEvent> item) {
                // TODO requiredClassesをみて対象のクラスをリロードすべきかを判断する(対象のクラスに関係あるもののみにフィルターする)
                if (item.stream().anyMatch(event -> event.kind == ENTRY_MODIFY || event.kind == ENTRY_CREATE || event.kind == OVERFLOW)) {
                    updateStatus(Status.CHANGE_DETECTED);
                    // TODO 即座にリロードせず、CHANGE_DETECTEDに遷移するのみでもよいのでは
                    loadAsync();
                }
            }
            @Override
            public void onComplete() {
                shutdown();
            }
        });
    }

    public void shutdown() {
        updateStatus(Status.STOPPED);
        statusPublisher.close();
        waitLastProcessorExecutor.shutdown();
    }

    private final AtomicReference<Future<?>> loadJob = new AtomicReference<>();
    private synchronized void loadAsync() {
        final Future<?> currentJob = loadJob.getAndSet(workerExecutorService.submit(this::load));
        if (currentJob != null) currentJob.cancel(true);
    }
    private void load() {
        final var classLoader = classLoaderSupplier.get();
        classLoader.getLoadedPathPublisher().subscribe(new SimpleSubscriber<>() {
            @Override
            public void process(Path item) {
                requiredClasses.add(item);
                pathWatcher.addRecursively(item);
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


    private AtomicReference<Status> status = new AtomicReference<>();
    private SubmissionPublisher<Status> statusPublisher = new SubmissionPublisher<>();
    private void updateStatus(Status status) {
        requireNonNull(status);
        if (getStatus() == Status.STOPPED) return;
        statusPublisher.submit(status);
    }
    public Status getStatus() {
        return status.get();
    }


    public enum Status {
        INITIALIZED, LOADING, LOAD_SUCCEEDED, CHANGE_DETECTED, LOAD_FAILED, STOPPED
    }
}
