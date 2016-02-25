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
 *
 * @author Edward
 */
public interface ChatFuture<V> extends Future<V>{
       
    V getInterruptibly() throws InterruptedException, IOException;

    V getUninterruptibly() throws IOException;

    void addCallback(ChatCallback<V> callback);
}
