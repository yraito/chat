package webchat.client.blocking;

/**
 * A subscriber of events in a chat room (aka "chat channel")
 * 
 * @author Nick
 *
 */
public interface ChannelListener {

    default void onJoin( String src) { }

    default void onLeave(String src, String newOwnr) { }
	
    default void onMessage( String src, String msg) { }
	
    default void onWhisper( String src, String msg) { }
	
    default void onKick( String src, String tgt, String rsn) { }
	
    default void onGrantToken( String tgt) { }
	
    default void onRevokeToken( String tgt) { }
	
    default void onNewOwner( String tgt) { }
    
    default void onClose( String rsn) { } 
}
