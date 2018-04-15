package com.gihtub.am4dr.javafx.sample_viewer.util;

import java.util.concurrent.*;

public class WaitLastProcessor<T> implements Flow.Processor<T, T> {

    private final SubmissionPublisher<T> publisher = new SubmissionPublisher<>();
    private final ScheduledExecutorService scheduledExecutorService;
    private final long time;
    private final TimeUnit unit;

    public WaitLastProcessor(ScheduledExecutorService scheduledExecutorService, long time, TimeUnit unit) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.time = time;
        this.unit = unit;
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        publisher.subscribe(subscriber);
    }

    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    private ScheduledFuture<?> scheduledFuture;
    @Override
    public void onNext(T item) {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
        }
        scheduledFuture = scheduledExecutorService.schedule(() -> {
            publisher.submit(item);
        }, time, unit);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        publisher.closeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        publisher.close();
    }
}
