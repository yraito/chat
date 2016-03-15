package webchat.client.http;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import webchat.client.*;
import webchat.core.*;
import webchat.servlet.api.Formatter;
import webchat.util.StringUtils;

public class HttpMessageSender implements Closeable, AutoCloseable {

    private final static Logger logger = LoggerFactory.getLogger(HttpMessageSender.class);

    Formatter formatter;
    String commandUri;
    AtomicReference<MessageChannel> chatSession;
    AtomicReference<ChatHandler> chatHandler;
    CloseableHttpAsyncClient httpclient;
    HttpClientContext httpContext;
    ListeningExecutorService execService;

    public HttpMessageSender(HttpClientContext httpContext, String commandUrl, Formatter formatter) {
        logger.info("Creating new Http sender, command url: {}", commandUrl);
        this.httpContext = httpContext;
        this.commandUri = commandUrl;
        this.formatter = formatter;
        execService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
    }

    public ChatFuture<ResultMessage> writeFuture(CommandMessage msg) {
        HttpPost httpReq = convertToHttpRequest(msg);
        SettableFuture<ResultMessage> futureResult = SettableFuture.create();
        logger.debug("Sending command over http request {} : {} : {}", httpReq.getRequestLine(), httpReq.getAllHeaders(), httpReq.toString());
        httpclient.execute(httpReq, httpContext, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse t) {
                try {
                    futureResult.set(convertFromHttpResponse(t));
                } catch (IOException e) {
                    futureResult.setException(e);
                }
            }

            @Override
            public void failed(Exception excptn) {
                futureResult.setException(excptn);
            }

            @Override
            public void cancelled() {

            }
        });
        return new FutureAdapter(futureResult);
    }

    private ResultMessage convertFromHttpResponse(HttpResponse hr) throws ProtocolException, IOException {
       logger.debug("Creating ResultMessage from Http response: {}", hr);
        try (InputStream is = hr.getEntity().getContent()) {
            int code = hr.getStatusLine().getStatusCode();
            if (code / 200 >= 2) {
                String body = StringUtils.readFully(is);
                throw new ProtocolException(hr.getStatusLine() + ":" + body);
            } else {
                Message msg = formatter.readMessage(is);
                if (!(msg instanceof ResultMessage)) {
                    throw new ProtocolException("Incorrect type, expecting ResultMessage: " + msg);
                }
                return (ResultMessage) msg;
            }
        }
    }

    private HttpPost convertToHttpRequest(CommandMessage cmdMsg) {
        logger.debug("Creating Http request from CommandMessage, URL: {} ; {}", cmdMsg, commandUri);
        HttpPost httpReq = new HttpPost(commandUri);
        LinkedList<NameValuePair> paramList = new LinkedList<>();
        paramList.add(new BasicNameValuePair("command", cmdMsg.getCommand()));
        if (cmdMsg.getRoomName() != null) {
            paramList.add(new BasicNameValuePair("room", cmdMsg.getRoomName()));
        }
        if (cmdMsg.getTargetName() != null) {
            paramList.add(new BasicNameValuePair("target", cmdMsg.getTargetName()));
        }
        if (cmdMsg.getMessage() != null) {
            paramList.add(new BasicNameValuePair("message", cmdMsg.getMessage()));
        }
        for (String arg : cmdMsg.getOtherArgs()) {
            paramList.add(new BasicNameValuePair("args[]", arg));
        }
        try {
            httpReq.setEntity(new UrlEncodedFormEntity(paramList));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        return httpReq;
    }

    public void close() {
        logger.debug("closing HttpCommandSender");
        try {
            httpclient.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }


}
