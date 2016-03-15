/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql.query;

import webchat.dao.*;
import webchat.dao.sql.MySQLDaoConnectionFactory;
import webchat.dao.dto.*;
import java.lang.reflect.Field;
import java.util.List;
/**
 *
 * @author Edward
 */
public class DerbyQuerySyntax implements QuerySyntax{

    @Override
    public String leftJoin() {
        return "LEFT OUTER JOIN";
    }

    @Override
    public String limit(int start, int num) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static void main(String[] args) throws Exception {
        DaoConnectionFactory daoFactory = MySQLDaoConnectionFactory.getInstance();
        try (DaoConnection dc = daoFactory.openDaoConnection()) {
            Searcher<UserRecord> searcher = dc.getUserDao().searcher();
            Field f = UserRecord.class.getDeclaredField("username");
            Where<UserRecord> w = searcher.whereLwrEq(f, "yoyoma");
            List<UserRecord> users = w.doQuery();
            System.out.println(users);
        }
    }
}
