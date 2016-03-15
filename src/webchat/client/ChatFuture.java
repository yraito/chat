/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client;

import java.io.IOException;
import java.util.concurrent.Future;
import webchat.core.ResultMessage;

/**
 * An extension of the Future interface that supports callbacks
 * 
 * @author Nick
 */
public interface ChatFuture<V> extends Future<V>{
       
    V getInterruptibly() throws InterruptedException, IOException;

    V getUninterruptibly() throws IOException;

    ChatFuture<V> addCallback(ChatCallback<V> callback);
}
