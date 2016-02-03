package webchat.dao;

/**
 * Entry point for the DAO layer. 
 * 
 * @author Nick
 */
public interface DaoConnectionFactory {

	DaoConnection openDaoConnection() throws DaoException;
}
