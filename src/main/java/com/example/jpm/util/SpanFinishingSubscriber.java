package com.example.jpm.util;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.log.Fields;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class SpanFinishingSubscriber<T> implements CoreSubscriber<T>, Subscription {

    private final CoreSubscriber<? super T> subscriber;
    private final Span span;
    private final Context context;
    private volatile Subscription subscription;
    private final AtomicBoolean completed;

    private final Tracer tracer;

    public SpanFinishingSubscriber(CoreSubscriber<? super T> subscriber, Span span) {
        this.subscriber = subscriber;
        this.span = span;
        completed = new AtomicBoolean();
        tracer = GlobalTracer.get();
        context = subscriber.currentContext().put(subscriber.getClass(), span);
    }

    @Override
    public void onSubscribe(Subscription s) {
        subscription = s;
        try (Scope scope = tracer.activateSpan(span)) {
            subscriber.onSubscribe(this);
        }
    }

    @Override
    public void onNext(T t) {
        try (Scope scope = tracer.activateSpan(span)) {
            subscriber.onNext(t);
        }
    }

    @Override
    public void onError(Throwable t) {
        if (null != span && completed.compareAndSet(false, true)) {
            span.setTag(Tags.ERROR, true);
            span.log(Map.of(Fields.ERROR_OBJECT, t, Fields.MESSAGE, t.getMessage()));
            span.finish();
        }
        subscriber.onError(t);
    }

    @Override
    public void onComplete() {
        if (null != span && completed.compareAndSet(false, true)) {
            span.finish();
        }
        subscriber.onComplete();
    }

    @Override
    public Context currentContext() {
        return context;
    }

    @Override
    public void request(long n) {
        subscription.request(n);
    }

    @Override
    public void cancel() {
        if (null != span && completed.compareAndSet(false, true)) {
            span.finish();
        }
        subscription.cancel();
    }
}