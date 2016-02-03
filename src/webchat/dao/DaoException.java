package webchat.dao;

import java.io.IOException;

/**
 * An exception thrown by the dao layer
 * 
 * @author Nick
 */
public class DaoException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DaoException(Throwable cause) {
		super(cause);
	}
	
	public DaoException(String message) {
		super(message);
	}

        public DaoException(String message, Throwable cause) {
            super(message, cause);
        }
}
