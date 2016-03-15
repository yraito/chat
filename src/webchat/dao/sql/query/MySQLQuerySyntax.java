/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webchat.dao.sql.query;

import java.util.List;
import webchat.dao.sql.TblInfo;

/**
 *
 * @author Edward
 */
public class MySQLQuerySyntax implements QuerySyntax{

    @Override
    public String leftJoin() {
        return "LEFT JOIN";
    }

    @Override
    public String limit(int start, int num) {
        return "LIMIT " + start + ", " + num;
    }
    
}
