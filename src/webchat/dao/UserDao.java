package webchat.dao;


import webchat.dao.dto.UserRecord;

public interface UserDao {
		
	void save(UserRecord user) throws DaoException;
	
	UserRecord find(String name) throws DaoException;

	Searcher<UserRecord> searcher();
}
