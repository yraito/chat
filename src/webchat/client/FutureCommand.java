/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.client;

import java.io.IOException;
import java.util.concurrent.Future;
import webchat.core.*;

/**
 *
 * @author Edward
 */
public interface FutureCommand {

    boolean isDone();

    boolean isCancelled();

    boolean cancel(boolean interruptRunning);

    ResultMessage get() throws InterruptedException, IOException;

    ResultMessage getUninterruptibly() throws IOException;

    void addCallback(ChatCallback callback);
}
