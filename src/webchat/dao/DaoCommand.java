package webchat.dao;

public interface DaoCommand {

	Object execute(DaoConnection dc) throws DaoException;
}
