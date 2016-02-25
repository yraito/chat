/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.http;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public abstract class AbstractWrappingFuture<I, O> implements ListenableFuture<O> {

    ListenableFuture<I> wrappedFut;
    O result;

    public AbstractWrappingFuture(ListenableFuture<I> wrappedFut) {
        this.wrappedFut = wrappedFut;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return wrappedFut.cancel(mayInterruptIfRunning);
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
    public O get() throws InterruptedException, ExecutionException {
        try {
            if (result == null) {
                result = get(wrappedFut.get());
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public O get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            if (result == null) {
                result = get(wrappedFut.get(timeout, unit));
            }
            return result;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        wrappedFut.addListener(listener, executor);
    }

    protected abstract O get(I inner) throws Exception;

}
