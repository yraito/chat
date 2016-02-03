package webchat.core;


public class ChatException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChatException(Throwable cause) {
		super(cause);
	}
	
	public ChatException(String err) {
		super(err);
	}
	
	public ChatException(String err, Throwable t) {
		super(err, t);
	}
}
