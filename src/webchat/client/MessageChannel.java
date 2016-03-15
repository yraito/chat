package webchat.client;



import java.io.Closeable;
import webchat.core.*;


/**
 * Represents a client's connection to the server. 
 * @author Nick
 *
 */
public interface MessageChannel extends Closeable{

	ChatFuture<ResultMessage> writeFuture(CommandMessage msg);
	
	void close();
}
