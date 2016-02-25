package webchat.client;

import webchat.core.ResultMessage;

public interface ChatCallback<V> {

    default void onCompleted(V v) {}
    
    default void onError(Throwable t) {}

}
