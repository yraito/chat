package webchat.client;

import webchat.core.Message;

/**
 * 
 * @author Nick
 *
 */
public interface ChatHandler {

    default void onChannelOpened(MessageChannel ioc) { }
	
    default void onChannelClosed(MessageChannel ioc) { }
	
    default void onMessageReceived(MessageChannel ioc, Message cm) { }
	
    default void onException(MessageChannel ioc, Throwable t) { }
	
}
