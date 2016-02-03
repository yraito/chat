package webchat.dao;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Represents a "where" clause/restrict operation on the table.  Can be composed with other
 * Where objects to obtain a more complex condition for the query.  
 * 
 * @author Nick
 *
 * @param <T>
 */
public interface Where<T> {
	
	Where<T> and(Where<T> that);
	
	Where<T> or(Where<T> that);
	
	List<T> doQuery() throws DaoException;

	List<T> doQuery(Field sortBy, Integer start, Integer num) throws DaoException;
}
