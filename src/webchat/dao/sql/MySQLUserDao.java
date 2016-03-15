package webchat.dao.sql;

import java.sql.Connection;

import webchat.dao.*;
import webchat.dao.dto.*;

public class MySQLUserDao extends MySQLGenericDao<UserRecord> implements UserDao {

    public MySQLUserDao(Mapping insertMapping, Mapping selectMapping, Connection conn) {
        super(insertMapping, selectMapping, conn, "username", UserRecord.class);
    }

    
    
}
