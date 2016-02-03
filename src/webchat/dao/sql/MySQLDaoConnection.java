package webchat.dao.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webchat.dao.*;
import webchat.dao.dto.AdminRecord;
import webchat.dao.dto.UserRecord;

public class MySQLDaoConnection implements DaoConnection {

    private final static Logger logger = LoggerFactory.getLogger(MySQLDaoConnection.class);

    static Mapper mapper = new Mapper();
    static Mapping adminSelect = mapper.getMappingForSelect(AdminRecord.class);
    static Mapping userInsert = mapper.getMappingForInsert(UserRecord.class);
    static Mapping userSelect = mapper.getMappingForSelect(UserRecord.class);
    static Mapping eventInsert = mapper.getMappingForInsert(UserRecord.class);
    static Mapping eventSelect = mapper.getMappingForSelect(UserRecord.class);

    Connection conn;
    
    public MySQLDaoConnection(Connection conn) {
        super();
        this.conn = conn;
        logger.debug("new MySQLDaoConnection wrapping Connection: {}", conn);
    }

    @Override
    public UserDao getUserDao() {
        return new MySQLUserDao(userInsert, userSelect, conn);
    }

    @Override
    public AdminDao getAdminDao() {
        return new MySQLAdminDao(adminSelect, conn);
    }

    @Override
    public EventDao getEventDao() {
        return new MySQLEventDao(eventInsert, eventSelect, conn);
    }

    @Override
    public Object executeTransaction(DaoCommand dc) throws DaoException {
        try {
            try {
                conn.setAutoCommit(false);
                Object rslt = dc.execute(this);
                conn.commit();
                return rslt;
            } catch (Exception e) {

                conn.rollback();
                throw new DaoException(e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
