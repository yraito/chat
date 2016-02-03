package webchat.core;

public class ConfigurationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(Throwable t) {
		super(t);
	}
	
	public ConfigurationException(String msg, Throwable t) {
		super(msg, t);
	}
}
