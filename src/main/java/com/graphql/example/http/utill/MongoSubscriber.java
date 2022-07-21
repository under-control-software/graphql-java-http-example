package com.graphql.example.http.utill;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class MongoSubscriber<T> implements Subscriber<T> {
    
    protected MongoSubscriber() {
    }

    @Override
    public void onSubscribe(final Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(final T t) {
    }

    @Override
    public void onError(final Throwable t) {
        onComplete();
    }

    @Override
    public void onComplete() {
    }

}