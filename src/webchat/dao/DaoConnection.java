package webchat.dao;

import java.io.Closeable;

public interface DaoConnection extends Closeable, AutoCloseable{

	UserDao getUserDao();
	
	AdminDao getAdminDao();
	
	EventDao getEventDao();
	
	Object executeTransaction(DaoCommand dc) throws DaoException;
	
	void close();
}
