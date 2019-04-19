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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
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

    private final PathWatchEventPublisher watchEventPublisher;
    private final SubmissionPublisher<PathWatcher.PathWatchEvent> filteredEventPublisher = new SubmissionPublisher<>();
    private final ScheduledExecutorService waitLastProcessorExecutor = Executors.newSingleThreadScheduledExecutor(DaemonThreadFactory.INSTANCE);

    // TODO PathWatcherとPathWatchEventPublisherはファサードにまとめていいかも
    public LatestInstanceProvider(String fqcn,
                                  Supplier<ReportingClassLoader> classLoaderSupplier,
                                  PathWatcherImpl pathWatcher,
                                  ExecutorService workerExecutorService,
                                  int delayTime) {
        this.fqcn = fqcn;
        this.classLoaderSupplier = classLoaderSupplier;
        this.pathWatcher = pathWatcher;
        this.workerExecutorService = workerExecutorService;
        updateStatus(Status.INITIALIZED);

        watchEventPublisher = new PathWatchEventPublisher(pathWatcher);
        watchEventPublisher.subscribe(new SimpleSubscriber<>() {
            @Override
            public void process(List<PathWatcher.PathWatchEvent> item) {
                item.stream()
                        // TODO requiredClassesをみて対象のクラスをリロードすべきかを判断する(対象のクラスに関係あるもののみにフィルターする)
                        .filter(event -> event.kind == ENTRY_CREATE || event.kind == OVERFLOW)
                        .forEach(filteredEventPublisher::submit);
            }
        });
        final WaitLastProcessor<PathWatcher.PathWatchEvent> waitLastPathEventProcessor =
                new WaitLastProcessor<>(waitLastProcessorExecutor, delayTime, TimeUnit.MILLISECONDS);
        filteredEventPublisher.subscribe(waitLastPathEventProcessor);
        waitLastPathEventProcessor.subscribe(new SimpleSubscriber<>() {
            @Override
            public void process(PathWatcher.PathWatchEvent item) {
                updateStatus(Status.CHANGE_DETECTED);
                // TODO 即座にリロードせず、CHANGE_DETECTEDに遷移するのみでもよいのでは
                //      その場合には特に複数のサブスクライバがいるときに誰がロードを要求するのかという問題は出るが
                loadAsync();
            }
            @Override
            public void onComplete() {
                shutdown();
            }
        });

        loadAsync();
    }

    public void shutdown() {
        updateStatus(Status.STOPPED);
        statusPublisher.close();
        waitLastProcessorExecutor.shutdown();
        watchEventPublisher.shutdown();
        filteredEventPublisher.close();
    }

    private final AtomicReference<Future<?>> loadJob = new AtomicReference<>();
    private synchronized void loadAsync() {
        final Future<?> currentJob = loadJob.getAndSet(workerExecutorService.submit(this::load));
        if (currentJob != null) currentJob.cancel(true);
    }
    private synchronized void load() {
        final var classLoader = classLoaderSupplier.get();
        classLoader.getLoadedPathPublisher().subscribe(new SimpleSubscriber<>() {
            @Override
            public void process(Path item) {
                requiredClasses.add(item);
                pathWatcher.addRecursively(item);
            }
        });

        updateStatus(Status.LOADING);
        classLoader.load(fqcn).map(clazz -> {
            try {
                final Object instance = clazz.getDeclaredConstructor().newInstance();
                return instance.getClass().getClassLoader() == classLoader ? instance : null;
            } catch (Throwable e) {
                return null;
            }
        }).ifPresentOrElse(instance -> {
            updateInstance(instance, classLoader);
            updateStatus(Status.LOAD_SUCCEEDED);
        }, () -> {
            updateStatus(Status.LOAD_FAILED);
            classLoader.shutdown();
        });
    }


    // TODO 最新のインスタンスとそのローダーを保持するものとしてinternalに抽出(LatestInstanceHolder?)
    private final AtomicReference<LoadedInstance> atomicInstance = new AtomicReference<>();
    public Optional<Object> getInstance() {
        return Optional.ofNullable(atomicInstance.get()).map(it -> it.instance);
    }
    private synchronized void updateInstance(Object instance, ReportingClassLoader reportingClassLoader) {
        final var oldInstance = atomicInstance.getAndSet(new LoadedInstance(instance, reportingClassLoader));
        if (oldInstance != null) oldInstance.dispose();
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


    private final AtomicReference<Status> status = new AtomicReference<>();
    private final SubmissionPublisher<Status> statusPublisher = new SubmissionPublisher<>();
    private synchronized void updateStatus(Status status) {
        requireNonNull(status);
        //System.out.println(this.getClass().getName()+": "+status);
        if (getStatus() == Status.STOPPED) return;
        this.status.set(status);
        statusPublisher.submit(status);
    }
    public Status getStatus() {
        return status.get();
    }
    public Flow.Publisher<Status> getStatusPublisher = statusPublisher;


    public enum Status {
        INITIALIZED, LOADING, LOAD_SUCCEEDED, CHANGE_DETECTED, LOAD_FAILED, STOPPED
    }
}
