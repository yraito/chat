package webchat.core;

import java.io.IOException;

/**
 * 
 * A problem with the underlying http transport, eg socket exceptions, 
 * non 200 responses, non auth
 * @author Nick
 *
 */
public class ConnectionException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConnectionException(String msg) {
		super(msg);
	}
	
	public ConnectionException(Throwable msg) {
		super(msg);
	}

}
