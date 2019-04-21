package com.github.am4dr.javafx.sample_viewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Flow;

final class FlowCollector<T> implements Flow.Subscriber<T> {

    private final List<T> list;
    private Flow.Subscription subscription;

    public FlowCollector() {
        this(Collections.synchronizedList(new ArrayList<>()));
    }

    public FlowCollector(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return Collections.unmodifiableList(list);
    }

    public Optional<T> getLast() {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size()-1));
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(T item) {
        list.add(item);
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }
}
