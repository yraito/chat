package webchat.client.http;

import java.io.Closeable;
import java.io.EOFException;
import static webchat.util.Util.*;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
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

public class HttpMessageReceiver implements Closeable {

    private final static Logger logger = LoggerFactory.getLogger(HttpMessageReceiver.class);

    private final HttpContext httpContext;
    private final String streamUrl;
    private final Formatter formatter;
    private AtomicReference<CloseableHttpAsyncClient> httpclient = new AtomicReference<>();
    private AtomicReference<PipedInputStream> pipeInput = new AtomicReference<>();
    private AtomicReference<WritableByteChannel> pipedOutputChannel = new AtomicReference<>();
    private AtomicReference<MessageChannel> chatSess = new AtomicReference<>();
    private AtomicReference<ChatHandler> chatHandler = new AtomicReference<>();
    private Thread streamRecvThread;
    private boolean isRunning;

    public HttpMessageReceiver(HttpContext httpContext, String url, Formatter formatter) {
        this.httpContext = httpContext;
        this.streamUrl = url;
        this.formatter = formatter;
    }

    public synchronized void start(MessageChannel chatSess, ChatHandler chatHandler) {
        logger.info("Starting HttpMessageReceiver for {}", streamUrl);
        this.chatSess.set(chatSess);
        this.chatHandler.set(chatHandler);
        httpclient.set(HttpAsyncClients.createDefault());
        httpclient.get().start();
        streamRecvThread = new Thread(() -> {
            long lastPoll = 0;
            long minInterval = 3000;

            while (isRunning()) {
                long thisInterval = System.currentTimeMillis() - lastPoll;
                if (thisInterval < minInterval) {
                    try {
                        Thread.sleep(minInterval - thisInterval);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                lastPoll = System.currentTimeMillis();
                HttpPost get = new HttpPost(streamUrl + "?t=" + Long.toString(lastPoll));

                try {
                    pipeInput.set(new PipedInputStream());
                    pipedOutputChannel.set(Channels.newChannel(new PipedOutputStream(pipeInput.get())));
                    writeReadDispatch(get);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    continue;
                } finally {
                    closeQuietly(pipeInput.get());
                    closeQuietly(pipedOutputChannel.get());
                }

            }
        });
        isRunning = true;
        streamRecvThread.start();

    }

    public synchronized void close() {
        if (streamRecvThread != null) {
            streamRecvThread.interrupt();
        }
        closeQuietly(pipeInput.get());
        closeQuietly(pipedOutputChannel.get());
        closeQuietly(httpclient.get());
        isRunning = false;
    }

    public synchronized boolean isRunning() {
        return isRunning;
    }

    private void writeReadDispatch(HttpUriRequest req) {

        logger.debug("Sending new Http request for stream: {}", req);
        try {

            AsyncByteConsumer<HttpResponse> consumer = new AsyncByteConsumer<HttpResponse>() {
                HttpResponse resp;

                @Override
                protected void onByteReceived(ByteBuffer arg0, IOControl arg1) throws IOException {
                    while (arg0.hasRemaining()) {
                        pipedOutputChannel.get().write(arg0);
                    }
                }

                @Override
                protected HttpResponse buildResult(HttpContext arg0) throws Exception {
                    pipedOutputChannel.get().close();
                    return resp;
                }

                @Override
                protected void onResponseReceived(HttpResponse arg0) throws HttpException, IOException {
                    this.resp = arg0;
                }
            };

            HttpAsyncRequestProducer producer = HttpAsyncMethods.create(req);
            httpclient.get().execute(producer, consumer, httpContext, null);
            ObjectInputStream ois = formatter.createReader(pipeInput.get());
            while (isRunning()) {
                Object msg = ois.readObject();
                List<Message> cmdList = null;
                if (msg instanceof CommandMessage) {
                    cmdList = Collections.singletonList((CommandMessage) msg);
                } else if (msg instanceof MessageListMessage) {
                    cmdList = ((MessageListMessage) msg).getMessages();
                } else {
                    logger.warn("Unexpected object from receiver stream. {}: {}", msg.getClass(), msg);
                    continue;
                }
                for (Message m : cmdList) {
                    if (m instanceof CommandMessage) {
                        CommandMessage cmdMsg = (CommandMessage) m;
                        logger.debug("Received CommandMessage: {}", cmdMsg);
                        chatHandler.get().onMessageReceived(chatSess.get(), cmdMsg);
                    } else {
                        logger.warn("Message {} is not a CommandMessage", m);
                    }
                }

            }

        } catch (EOFException e) {
            logger.debug("ObjectInputStream EOF");
        } catch (IOException e) {
            e.printStackTrace();
            chatHandler.get().onException(chatSess.get(), e);

        } catch (ClassNotFoundException ex) {
            logger.error(ex.getMessage());
        }
    }
}
