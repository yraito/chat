/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.core;

import java.io.IOException;
/**
 *
 * @author Edward
 */
public class AuthException extends IOException {
    	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthException(Throwable cause) {
		super(cause);
	}
	
	public AuthException(String err) {
		super(err);
	}
	
	public AuthException(String err, Throwable t) {
		super(err, t);
	}
}
