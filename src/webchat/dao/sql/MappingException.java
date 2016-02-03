/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql;

/**
 * Exception performing class to table mapping
 * @author Nick
 */
public class MappingException extends RuntimeException {
    
    public MappingException(String message) {
        super(message);
    }
    
    public MappingException(Throwable cause) {
        super(cause);
    }
    
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
