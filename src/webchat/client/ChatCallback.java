package webchat.client;

import webchat.core.ResultMessage;

/**
 * Callback functions that can be attached to a ChatFuture
 * @author Nick
 * @param <V> 
 */
public interface ChatCallback<V> {

    default void onCompleted(V v) {}
    
    default void onError(Throwable t) {}

}
