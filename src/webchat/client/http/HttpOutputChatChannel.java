package webchat.client.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.client.*;
import webchat.core.*;
import webchat.core.command.LogoutCommand;
import webchat.servlet.api.Formatter;
import webchat.servlet.api.XStreamFormatter;

public class HttpOutputChatChannel implements ChatSession {

    private final static Logger logger = LoggerFactory.getLogger(HttpOutputChatChannel.class);

    Formatter formatter = new XStreamFormatter();
    URI commandUri;
    CloseableHttpAsyncClient httpclient;
    HttpClientContext httpContext;

    public HttpOutputChatChannel(HttpClientContext httpContext, String commandUrl) {
        this.httpContext = httpContext;
        this.commandUri = URI.create(commandUrl);
        httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
    }

    @Override
    public Future<ResultMessage> writeFuture(CommandMessage msg, ResultCallback rc) {
        logger.debug("Preparing to write CommandMessage: {} to {}", msg, commandUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            formatter.writeMessage(msg, baos);
            ByteArrayEntity bae = new ByteArrayEntity(baos.toByteArray());
            HttpPost req = new HttpPost(commandUri);
            req.setEntity(bae);
            logger.debug("Sending request {} : {} : {}", req.getRequestLine(), req.getAllHeaders(), new String(baos.toByteArray()));
            Future<HttpResponse> resp = httpclient.execute(req, httpContext, null);
            return new ResultMessageWrappingFuture(resp, formatter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        logger.debug("closing Http output channel");
        try {
            writeFuture(new LogoutCommand(), null);
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ResultMessageWrappingFuture extends HttpWrappingFuture<ResultMessage> {

        Formatter fmt;

        public ResultMessageWrappingFuture(Future<HttpResponse> innerFut, Formatter fmt) {
            super(innerFut, ResultMessage.class);
            this.fmt = fmt;
        }

        @Override
        protected ResultMessage convert(InputStream is, HttpResponse hr) throws IOException {
            return (ResultMessage) fmt.readMessage(is);
        }

    }

}
