package webchat.core;
import java.io.IOException;

/**
 * An abstraction representing the server-side endpoint of a client-server connection, which
 * allows implementations to plug in different transport methods, e.g. HTTP or TCP socket.
 * 
 * @author Nick
 *
 */
public interface ClientSession {

	
	/**
	 * Account id of this session's client
	 * @return
	 * 
	 */
	int getUserId();
	
	/**
	 * Username of this session's client
	 * @return
	 */
	String getUserName();
	
	/**
	 * Set a "session variable" associated with this client's session.
	 * 
	 * @param key
	 * @param value
	 * @return the previous value, or null if there was none
	 */
	Object attach(String key, Object value);
	
	/**
	 * Get a "session variable" that was set with attach()
	 * 
	 * @param key
	 * @return the value, or null if no such variable
	 */
	Object getAttachment(String key);
	
	/**
	 * Delete a "session variable" that was set with attach()
	 * @param key
	 * @return
	 */
	Object detach(String key);
	
	/**
	 * Send a message to this client
	 * @param message
	 * @throws IOException
	 */
	void writeMessage(Message message) throws IOException;
	
	/**
	 * 
	 * @return
	 */
	boolean isClosed();
	
	/**
	 * Close the connection. Client will need to open a new 
	 * session
	 */
	void closeSession();
}
