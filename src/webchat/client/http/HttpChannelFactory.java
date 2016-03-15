/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.http;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webchat.client.ChannelFactory;
import webchat.client.ChatFuture;
import webchat.client.ChatHandler;
import webchat.client.MessageChannel;
import webchat.servlet.api.Formatter;

/**
 *
 * @author Nick
 */
public class HttpChannelFactory implements ChannelFactory {

    private final static Logger logger = LoggerFactory.getLogger(HttpChannelFactory.class);
    
    private final String streamUrl;
    private final String commandUrl;
    private final Formatter formatter;

    public HttpChannelFactory(String commandUrl, String streamUrl, Formatter formatter) {
        this.streamUrl = streamUrl;
        this.commandUrl = commandUrl;
        this.formatter = formatter;
    }

    @Override
    public ChatFuture<MessageChannel> open(ChatHandler ioh) throws IOException {
        logger.info("Opening Http MessageChannel");
        SettableFuture<MessageChannel> futureChan = SettableFuture.create();
        Runnable r = () -> {
            HttpClientContext httpContext = HttpClientContext.create();
            httpContext.setCookieStore(new BasicCookieStore());
            HttpMessageSender sender = new HttpMessageSender(httpContext, commandUrl, formatter);
            HttpMessageReceiver receiver = new HttpMessageReceiver(httpContext, streamUrl, formatter);
            HttpMessageChannel chatSess = new HttpMessageChannel(sender, receiver, ioh);
            chatSess.start();
            ioh.onChannelOpened(chatSess);
            futureChan.set(chatSess);
        };
        new Thread(r).start();
        return new FutureAdapter(futureChan);
    }
}
