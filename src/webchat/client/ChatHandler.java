package webchat.client;

import webchat.core.Message;

/**
 * 
 * @author Nick
 *
 */
public interface ChatHandler {

    default void onSessionOpened(ChatSession ioc) { }
	
    default void onSessionClosed(ChatSession ioc) { }
	
    default void onMessageReceived(ChatSession ioc, Message cm) { }
	
    default void onException(ChatSession ioc, Throwable t) { }
	
}
