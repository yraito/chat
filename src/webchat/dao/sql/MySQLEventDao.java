package webchat.dao.sql;

import java.sql.Connection;

import webchat.dao.*;
import webchat.dao.dto.*;

public class MySQLEventDao extends MySQLGenericDao<EventRecord> implements EventDao {

    public MySQLEventDao(Mapping insertMapping, Mapping selectMapping, Connection conn) {
        super(insertMapping, selectMapping, conn, null, EventRecord.class);
    }


}
