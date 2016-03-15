/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.http;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import webchat.client.ChatCallback;
import webchat.client.ChatFuture;

/**
 *
 * @author Nick
 */
public class FutureAdapter<V> implements ChatFuture<V> {

    ListenableFuture<V> wrappedFut;

    public FutureAdapter(ListenableFuture<V> wrappedFut) {
        this.wrappedFut = wrappedFut;
    }

    @Override
    public boolean cancel(boolean interruptRunning) {
        return wrappedFut.cancel(interruptRunning);
    }

    @Override
    public boolean isCancelled() {
        return wrappedFut.isCancelled();
    }

    @Override
    public boolean isDone() {
        return wrappedFut.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return wrappedFut.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return wrappedFut.get(timeout, unit);
    }

    @Override
    public V getInterruptibly() throws InterruptedException, IOException {
        try {
            return wrappedFut.get();
        } catch (ExecutionException e) {
            Throwable ec = e.getCause();
            if (ec instanceof IOException) {
                throw (IOException) ec;
            }
            throw new RuntimeException(ec);
        }
    }

    @Override
    public V getUninterruptibly() throws IOException {
        try {
            return Uninterruptibles.getUninterruptibly(wrappedFut);
        } catch (ExecutionException e) {
            Throwable ec = e.getCause();
            if (ec instanceof IOException) {
                throw (IOException) ec;
            }
            throw new RuntimeException(ec);
        }
    }

    @Override
    public ChatFuture<V> addCallback(ChatCallback<V> callback) {
        Futures.addCallback(wrappedFut, new CallbackAdapter(callback));
        return this;
    }

}
