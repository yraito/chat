package webchat.core;

import java.io.IOException;


public class ProtocolException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ProtocolException(String msg) {
		super(msg);
	}

	public ProtocolException(Throwable msg) {
		super(msg);
	}
}
