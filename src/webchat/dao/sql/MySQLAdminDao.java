package webchat.dao.sql;

import java.sql.Connection;

import webchat. dao.AdminDao;
import webchat.dao.dto.AdminRecord;

public class MySQLAdminDao extends MySQLGenericDao<AdminRecord> implements AdminDao {
	
    public MySQLAdminDao(Mapping selectMapping, Connection conn) {
        super(selectMapping, selectMapping, conn, "username", AdminRecord.class);
    }
	
}
