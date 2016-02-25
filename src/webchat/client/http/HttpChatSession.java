/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client.http;

import java.util.concurrent.Future;
import webchat.client.*;
import webchat.core.CommandMessage;
import webchat.core.ResultMessage;
import webchat.core.command.LogoutCommand;


/**
 *
 */
public class HttpChatSession implements ChatSession {

    HttpMessageSender sender;
    HttpMessageReceiver receiver;
    ChatHandler chatHandler;

    public HttpChatSession(HttpMessageSender sender, HttpMessageReceiver receiver, ChatHandler chatHandler) {
        this.sender = sender;
        this.receiver = receiver;
        this.chatHandler = chatHandler;
    }
    
    
    @Override
    public ChatFuture<ResultMessage> writeFuture(CommandMessage msg) {
        ChatFuture<ResultMessage> futureCmd = sender.writeFuture(msg);
        futureCmd.addCallback(new ChatCallback() {
            public void onException(Throwable t) {
                chatHandler.onException(HttpChatSession.this, t);
            }
        });
        return futureCmd;
    }
    
    public void start() {
        receiver.start(this, chatHandler);
    }

    @Override
    public void close() {
        sender.writeFuture(new LogoutCommand());
        sender.close();
        receiver.close();
    }
    
    
}
