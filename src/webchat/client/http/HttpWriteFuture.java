package webchat.client.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;

import webchat.core.ConnectionException;
import webchat.core.Message;
import webchat.core.ProtocolException;
import webchat.core.ResultMessage;
import webchat.servlet.api.Formatter;
import webchat.util.StringUtils;

public abstract class HttpWriteFuture implements Future<ResultMessage> {

    Future<HttpResponse> innerFut;
    Formatter formatter;

    public static ResultMessage convertFromHttp(Formatter formatter, HttpResponse hr) throws ProtocolException, IOException{
        try (InputStream is = hr.getEntity().getContent()) {
            int code = hr.getStatusLine().getStatusCode();
            if (code / 200 >= 2) {
                String body = StringUtils.readFully(is);
                throw new ProtocolException(hr.getStatusLine() + ":" + body);
            } else {
                Message msg = formatter.readMessage(is);
                if ( !(msg instanceof ResultMessage)  ) {
                    throw new ProtocolException("Incorrect type, expecting ResultMessage: " + msg);
                }
                return (ResultMessage) msg;
            }
        }  
    }

    public HttpWriteFuture(Future<HttpResponse> innerFut, Formatter formatter) {
        this.innerFut = innerFut;
        this.formatter = formatter;
    }

    @Override
    public boolean cancel(boolean arg0) {
        return innerFut.cancel(arg0);
    }

    @Override
    public ResultMessage get() throws InterruptedException, ExecutionException {
        HttpResponse inner = innerFut.get();
        try {
            return convertFromHttp(formatter, inner);
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public ResultMessage get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
        HttpResponse inner = innerFut.get(arg0, arg1);
        try {
           return convertFromHttp(formatter, inner);
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

}
