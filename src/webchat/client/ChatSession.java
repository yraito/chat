package webchat.client;

import java.util.concurrent.Future;

import webchat.core.CommandMessage;
import webchat.core.ResultMessage;

/**
 * Represents a client's connection to the server. 
 * @author Nick
 *
 */
public interface ChatSession {

	Future<ResultMessage> writeFuture(CommandMessage msg, ResultCallback rc);
	
	void close();
	
}
