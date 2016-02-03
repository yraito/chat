package webchat.dao;


import webchat.dao.dto.EventRecord;

public interface EventDao {

	void save(EventRecord event) throws DaoException;
	
	Searcher<EventRecord> searcher();
}
