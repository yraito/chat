package webchat.client.http;

import static webchat.util.Util.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.client.*;
import webchat.core.*;
import webchat.servlet.api.Formatter;
import webchat.servlet.api.XStreamFormatter;

public class HttpInputStreamReceiver {

    private final static Logger logger = LoggerFactory.getLogger(HttpInputStreamReceiver.class);

    CloseableHttpAsyncClient httpclient;
    HttpContext httpContext;
    String streamUrl;
    PipedOutputStream pipedOutput;
    PipedInputStream pipeInput;
    WritableByteChannel pipedOutputChannel;
    ChatSession thisSess;
    ChatHandler chatHandler;
    Formatter formatter;
    boolean isRunning;
    Thread streamRecvThread;

    public HttpInputStreamReceiver(HttpContext httpContext, String url, ChatSession thisSess, ChatHandler chatHandler) {
        super();
        this.httpContext = httpContext;
        this.streamUrl = url;
        this.thisSess = thisSess;
        this.chatHandler = chatHandler;
        this.formatter = new XStreamFormatter();
    }

    public synchronized void start() {
        logger.info("Starting stream receiver for {}", streamUrl);
        httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
        pipeInput = new PipedInputStream();
        try {
            pipedOutput = new PipedOutputStream(pipeInput);
            pipedOutputChannel = Channels.newChannel(pipedOutput);
        } catch (IOException e) {
            stop();
            throw new RuntimeException(e);
        }

        streamRecvThread = new Thread(() -> {
            while (isRunning()) {
                HttpGet get = new HttpGet(streamUrl);
                writeReadDispatch(get);
            }
        });
        isRunning = true;
        streamRecvThread.start();

    }

    public synchronized void stop() {
        if (streamRecvThread != null) {
            streamRecvThread.interrupt();
        }
        closeQuietly(pipeInput);
        closeQuietly(pipedOutputChannel);
        closeQuietly(httpclient);
        isRunning = false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private void writeReadDispatch(HttpUriRequest req) {

        logger.debug("Sending request: {}, {}", req.getRequestLine(), req.getAllHeaders());
        try {

            AsyncByteConsumer<HttpResponse> consumer = new AsyncByteConsumer<HttpResponse>() {
                HttpResponse resp;

                @Override
                protected void onByteReceived(ByteBuffer arg0, IOControl arg1) throws IOException {
                    while (arg0.hasRemaining()) {
                        pipedOutputChannel.write(arg0);
                    }
                }

                @Override
                protected HttpResponse buildResult(HttpContext arg0) throws Exception {
                    return resp;
                }

                @Override
                protected void onResponseReceived(HttpResponse arg0) throws HttpException, IOException {
                    this.resp = arg0;
                }
            };

            HttpAsyncRequestProducer producer = HttpAsyncMethods.create(req);
            httpclient.execute(producer, consumer, httpContext, null);
            Object msg = formatter.readMessage(pipeInput);
            if (!(msg instanceof MessageListMessage)) {
                throw new ProtocolException("Unexpected message type from server: " + msg);
            }
            MessageListMessage mlm = (MessageListMessage) msg;
            logger.debug("Received MessageListMessage");
            for (Message m : mlm.getMessages()) {
                logger.debug("MessageListMessage contained message: {}", m);
                chatHandler.onMessageReceived(thisSess, m);
            }

        } catch (IOException e) {
            chatHandler.onException(thisSess, e);
        }
    }

    
}
