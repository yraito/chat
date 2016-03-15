/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.http;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import webchat.client.*;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.command.*;

import org.slf4j.LoggerFactory;


/**
 *
 */
public class HttpMessageChannel implements MessageChannel {

    private final static Logger logger = LoggerFactory.getLogger(HttpMessageChannel.class);
    
    private final HttpMessageSender sender;
    private final HttpMessageReceiver receiver;
    private final ChatHandler chatHandler;
    private final ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();

    public HttpMessageChannel(HttpMessageSender sender, HttpMessageReceiver receiver, ChatHandler chatHandler) {
        this.sender = sender;
        this.receiver = receiver;
        this.chatHandler = chatHandler;
    }

    @Override
    public ChatFuture<ResultMessage> writeFuture(CommandMessage msg) {
        logger.debug("sending CommandMessage: {}", msg);
        return sender.writeFuture(msg).addCallback(new ChatCallback<ResultMessage>() {
            public void onException(Throwable t) {
                logger.debug("onException: {}", t.getMessage());
                chatHandler.onException(HttpMessageChannel.this, t);
            }           
        });
    }

    public void start() {
        Runnable r = () -> {
            logger.debug("Sending heartbeat");
            writeFuture(new HeartbeatCommand());
        };
        execService.scheduleAtFixedRate(r, 0, 30, TimeUnit.SECONDS);
        receiver.start(this, chatHandler);
    }

    @Override
    public void close() {
        execService.shutdown();
        sender.close();
        receiver.close();
    }
}
