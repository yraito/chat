package webchat.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;

import webchat.core.ConnectionException;
import webchat.core.ProtocolException;
import webchat.util.StringUtils;

public abstract class HttpWrappingFuture<O> implements Future<O> {

    Future<HttpResponse> innerFut;
    Class<O> clazz;

    public HttpWrappingFuture(Future<HttpResponse> innerFut, Class<O> clazz) {
        this.innerFut = innerFut;
        this.clazz = clazz;
    }

    @Override
    public boolean cancel(boolean arg0) {
        return innerFut.cancel(arg0);
    }

    @Override
    public O get() throws InterruptedException, ExecutionException {
        HttpResponse inner = innerFut.get();
        try {
            return convert(inner);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public O get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
        HttpResponse inner = innerFut.get(arg0, arg1);
        try {
            return convert(inner);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public boolean isCancelled() {
        return innerFut.isCancelled();
    }

    @Override
    public boolean isDone() {
        return innerFut.isDone();
    }

    protected O convert(HttpResponse hr) throws IOException {
        try (InputStream is = hr.getEntity().getContent()) {
            int code = hr.getStatusLine().getStatusCode();
            if (code / 200 != 2) {
                String body = StringUtils.readFully(is);
                throw new ConnectionException(hr.getStatusLine() + ":" + body);
            } else {
                return convert(is, hr);
            }

        } catch (ProtocolException e) {
            throw e;
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    protected abstract O convert(InputStream is, HttpResponse hr) throws IOException;
}
