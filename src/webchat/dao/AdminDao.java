package webchat.dao;

import webchat.dao.dto.AdminRecord;


public interface AdminDao {
	
	AdminRecord find(String name) throws DaoException;

}
